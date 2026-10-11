package com.myProject.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.myProject.order.dto.*;
import com.myProject.rest_client.dto.*;
import com.myProject.rest_client.service.DigitalBankingClientService;
import com.myProject.security.user.CurrentUserUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myProject.cart.entity.*;
import com.myProject.cart.repository.CartRepository;
import com.myProject.exception.*;
import com.myProject.order.dto.*;
import com.myProject.order.entity.*;
import com.myProject.order.repository.OrderRepository;
import com.myProject.product.entity.Product;
import com.myProject.product.repository.ProductRepository;
import com.myProject.user.entity.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final DigitalBankingClientService digitalBankingClientService;
    private final CurrentUserUtil currentUserUtil;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllOrders(pageable);
        return orders.map(order -> modelMapper.map(order, OrderResponse.class));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return modelMapper.map(order, OrderResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = currentUserUtil.getCurrentUser();
        List<Order> orders = user.getOrders();
        return orders.stream()
                    .map(order -> modelMapper.map(order, OrderResponse.class))
                    .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        validateOrderAuthority(order);
        if (request.getOrderStatus() == OrderStatus.SHIPPED && order.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new BadRequestException("Cannot ship an order with payment status " + order.getPaymentStatus() 
            + " and order status " + request.getOrderStatus());
        }
        order.setOrderStatus(request.getOrderStatus());
        Order savedOrder = orderRepository.save(order);
        return modelMapper.map(savedOrder, OrderResponse.class);
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        validateOrderAuthority(order);
        if (request.getPaymentStatus() == PaymentStatus.PAID && order.getOrderStatus() == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.PLACED);
        }
        order.setPaymentStatus(request.getPaymentStatus());
        Order savedOrder = orderRepository.save(order);
        return modelMapper.map(savedOrder, OrderResponse.class);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        User user = currentUserUtil.getCurrentUser();
        Long productId = orderRequest.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(()
                        -> new ResourceNotFoundException(
                        "Product not found with ID: " + productId));
        Integer quantity = orderRequest.getQuantity();
        PaymentMethod paymentMethod = user.getPaymentMethod();
        Integer stockQuantity = product.getStockQuantity();
        if (stockQuantity < quantity) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
            if (digitalBankingClientService.getMyAccounts().isEmpty()) {
                throw new ResourceNotFoundException("No active bank account for the user");
            }
            if (orderRequest.getAccountNumber() == null) {
                throw new ResourceNotFoundException("No account number found");
            }
            AccountResponse account = digitalBankingClientService.getAccountByNumber(orderRequest.getAccountNumber());
            if (account.getBalance().compareTo(totalAmount) < 0) {
                throw new BadRequestException("Insufficient balance in the account");
            }
            TransactionRequest withdrawRequest = TransactionRequest.builder()
                    .accountNumber(orderRequest.getAccountNumber())
                    .amount(totalAmount)
                    .build();
            digitalBankingClientService.withdraw(withdrawRequest);
        }
        Order order = Order.builder()
                .user(user)
                .seller(product.getUser())
                .address(orderRequest.getAddress() != null ? orderRequest.getAddress() : user.getAddress())
                .totalAmount(totalAmount)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(
                        paymentMethod == PaymentMethod.BANK_TRANSFER
                                ? PaymentStatus.PAID
                                : PaymentStatus.PENDING)
                .paymentMethod(paymentMethod)
                .orderItems(new ArrayList<>())
                .build();
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .priceAtPurchase(product.getPrice())
                .build();

        order.getOrderItems().add(orderItem);

        product.setStockQuantity(stockQuantity - quantity);

        productRepository.save(product);

        return modelMapper.map(orderRepository.save(order), OrderResponse.class);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional
    public List<OrderResponse> placeOrderFromCart() {
        User user = currentUserUtil.getCurrentUser();
        Cart cart = user.getCart();
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Order cannot be placed with an empty cart");
        }
        List<CartItem> cartItems = cart.getCartItems();
        PaymentMethod paymentMethod = user.getPaymentMethod();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
            totalAmount = totalAmount.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }
        if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
            if (digitalBankingClientService.getMyAccounts().isEmpty()) {
                throw new ResourceNotFoundException("No active bank account found for user");
            }
            AccountResponse primaryAccount = digitalBankingClientService.getMyAccounts().getFirst();
            if (primaryAccount.getBalance().compareTo(totalAmount) < 0) {
                throw new BadRequestException("Insufficient balance in account");
            }
            TransactionRequest withdrawRequest = TransactionRequest.builder()
                    .accountNumber(primaryAccount.getAccountNumber())
                    .amount(totalAmount)
                    .build();
            digitalBankingClientService.withdraw(withdrawRequest);
        }
        Map<User, List<CartItem>> itemsBySeller = cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getUser()));

        List<Order> savedOrders = new ArrayList<>();

        for (Map.Entry<User, List<CartItem>> entry : itemsBySeller.entrySet()) {
            User seller = entry.getKey();
            List<CartItem> sellerCartItems = entry.getValue();
            BigDecimal sellerOrderTotal = sellerCartItems.stream()
                    .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Order order = Order.builder()
                    .user(user)
                    .seller(seller)
                    .address(user.getAddress())
                    .paymentMethod(paymentMethod)
                    .orderDate(LocalDateTime.now())
                    .totalAmount(sellerOrderTotal)
                    .orderStatus(OrderStatus.PENDING)
                    .paymentStatus(paymentMethod == PaymentMethod.BANK_TRANSFER ? PaymentStatus.PAID : PaymentStatus.PENDING)
                    .orderItems(new ArrayList<>())
                    .build();
            for (CartItem cartItem : sellerCartItems) {
                Product product = cartItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(cartItem.getQuantity())
                        .priceAtPurchase(product.getPrice())
                        .build();
                order.getOrderItems().add(orderItem);
            }

            savedOrders.add(orderRepository.save(order));
        }
        cart.getCartItems().clear();
        cartRepository.save(cart);
        return savedOrders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional
    public void cancelOrder(Long orderId) {
        User currentUser = currentUserUtil.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AuthorizationDeniedException("You can only cancel your own orders");
        }
        if (order.getOrderStatus() == OrderStatus.DELIVERED
                || order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel an order with order status: " + order.getOrderStatus());
        }
        order.getOrderItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });
        if (order.getPaymentStatus() == PaymentStatus.PAID && currentUser.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            List<AccountResponse> accounts = digitalBankingClientService.getMyAccounts();
            if (accounts.isEmpty()) {
                throw new ResourceNotFoundException("No active bank account found for user");
            }
            AccountResponse primaryAccount = accounts.getFirst();
            TransactionRequest depositRequest = TransactionRequest.builder()
                    .accountNumber(primaryAccount.getAccountNumber())
                    .amount(order.getTotalAmount())
                    .build();
            digitalBankingClientService.deposit(depositRequest);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        order.setPaymentStatus(PaymentStatus.REFUNDED);

        orderRepository.save(order);
    }

    // Private method
    private void validateOrderAuthority(Order order) {
        Long sellerId = currentUserUtil.getCurrentUser().getId();
        if (!order.getSeller().getId().equals(sellerId)) {
            throw new AuthorizationDeniedException("You can only manage orders assigned to your seller account");
        }
    }
}
