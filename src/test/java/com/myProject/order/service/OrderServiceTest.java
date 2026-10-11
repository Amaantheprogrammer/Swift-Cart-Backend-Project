package com.myProject.order.service;

import com.myProject.cart.entity.Cart;
import com.myProject.cart.entity.CartItem;
import com.myProject.cart.repository.CartRepository;
import com.myProject.exception.BadRequestException;
import com.myProject.exception.ResourceNotFoundException;
import com.myProject.order.dto.OrderRequest;
import com.myProject.order.dto.OrderResponse;
import com.myProject.order.dto.OrderStatusUpdateRequest;
import com.myProject.order.dto.PaymentStatusUpdateRequest;
import com.myProject.order.entity.Order;
import com.myProject.order.entity.OrderItem;
import com.myProject.order.entity.OrderStatus;
import com.myProject.order.entity.PaymentStatus;
import com.myProject.order.repository.OrderRepository;
import com.myProject.product.entity.Product;
import com.myProject.product.repository.ProductRepository;
import com.myProject.rest_client.dto.AccountResponse;
import com.myProject.rest_client.dto.TransactionRequest;
import com.myProject.rest_client.service.DigitalBankingClientService;
import com.myProject.security.user.CurrentUserUtil;
import com.myProject.user.entity.PaymentMethod;
import com.myProject.user.entity.Role;
import com.myProject.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private DigitalBankingClientService digitalBankingClientService;

    @Mock
    private CurrentUserUtil currentUserUtil;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private User seller1;
    private User seller2;
    private Product product1;
    private Product product2;
    private Order sampleOrder;
    private OrderResponse sampleOrderResponse;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .name("Customer One")
                .email("customer@test.com")
                .password("password123")
                .role(Role.ROLE_USER)
                .address("123 Main Street")
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .orders(new ArrayList<>())
                .build();

        seller1 = User.builder()
                .id(10L)
                .name("Seller Alpha")
                .email("seller1@test.com")
                .password("sellerpass")
                .role(Role.ROLE_SELLER)
                .address("456 Market Lane")
                .build();

        seller2 = User.builder()
                .id(20L)
                .name("Seller Beta")
                .email("seller2@test.com")
                .password("sellerpass")
                .role(Role.ROLE_SELLER)
                .address("789 Industrial Rd")
                .build();

        product1 = Product.builder()
                .id(101L)
                .name("Wireless Mouse")
                .price(new BigDecimal("50.00"))
                .stockQuantity(10)
                .user(seller1)
                .build();

        product2 = Product.builder()
                .id(102L)
                .name("Mechanical Keyboard")
                .price(new BigDecimal("120.00"))
                .stockQuantity(5)
                .user(seller2)
                .build();

        sampleOrder = Order.builder()
                .id(501L)
                .user(customer)
                .seller(seller1)
                .totalAmount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .address("123 Main Street")
                .orderDate(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        sampleOrderResponse = OrderResponse.builder()
                .id(501L)
                .userId(customer.getId())
                .userName(customer.getName())
                .totalAmount(sampleOrder.getTotalAmount())
                .paymentMethod(sampleOrder.getPaymentMethod())
                .orderStatus(sampleOrder.getOrderStatus())
                .paymentStatus(sampleOrder.getPaymentStatus())
                .orderDate(sampleOrder.getOrderDate())
                .build();
    }

    // ==========================================
    // 1. getAllOrders Tests
    // ==========================================
    @Nested
    @DisplayName("getAllOrders Tests")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return a paginated list of OrderResponse when orders exist")
        void getAllOrders_WhenOrdersExist_ShouldReturnPageOfOrderResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(sampleOrder), pageable, 1);

            when(orderRepository.findAllOrders(pageable)).thenReturn(orderPage);
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            Page<OrderResponse> result = orderService.getAllOrders(pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            assertEquals(sampleOrderResponse.getId(), result.getContent().get(0).getId());

            verify(orderRepository).findAllOrders(pageable);
            verify(modelMapper).map(sampleOrder, OrderResponse.class);
        }

        @Test
        @DisplayName("Should return an empty page when no orders exist")
        void getAllOrders_WhenNoOrdersExist_ShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(orderRepository.findAllOrders(pageable)).thenReturn(emptyPage);

            Page<OrderResponse> result = orderService.getAllOrders(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());

            verify(orderRepository).findAllOrders(pageable);
            verifyNoInteractions(modelMapper);
        }
    }

    // ==========================================
    // 2. getOrderById Tests
    // ==========================================
    @Nested
    @DisplayName("getOrderById Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return OrderResponse when order is found by ID")
        void getOrderById_WhenOrderExists_ShouldReturnOrderResponse() {
            when(orderRepository.findOrderById(501L)).thenReturn(Optional.of(sampleOrder));
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            OrderResponse result = orderService.getOrderById(501L);

            assertNotNull(result);
            assertEquals(501L, result.getId());
            assertEquals(customer.getId(), result.getUserId());

            verify(orderRepository).findOrderById(501L);
            verify(modelMapper).map(sampleOrder, OrderResponse.class);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order does not exist")
        void getOrderById_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
            when(orderRepository.findOrderById(999L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.getOrderById(999L)
            );

            assertEquals("Order not found with ID: 999", exception.getMessage());
            verify(orderRepository).findOrderById(999L);
            verifyNoInteractions(modelMapper);
        }
    }

    // ==========================================
    // 3. getOrderByUserId Tests
    // ==========================================
    @Nested
    @DisplayName("getOrderByUserId Tests")
    class GetOrderByUserIdTests {

        @Test
        @DisplayName("Should return list of OrderResponse when orders found for user")
        void getOrderByUserId_WhenOrdersExist_ShouldReturnListOfOrderResponses() {
            when(orderRepository.findByUserId(customer.getId())).thenReturn(List.of(sampleOrder));
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            List<OrderResponse> result = orderService.getOrderByUserId(customer.getId());

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(501L, result.get(0).getId());

            verify(orderRepository).findByUserId(customer.getId());
            verify(modelMapper).map(sampleOrder, OrderResponse.class);
        }

        @Test
        @DisplayName("Should return empty list when no orders found for user")
        void getOrderByUserId_WhenNoOrders_ShouldReturnEmptyList() {
            when(orderRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

            List<OrderResponse> result = orderService.getOrderByUserId(999L);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(orderRepository).findByUserId(999L);
            verifyNoInteractions(modelMapper);
        }
    }

    // ==========================================
    // 4. getMyOrders Tests
    // ==========================================
    @Nested
    @DisplayName("getMyOrders Tests")
    class GetMyOrdersTests {

        @Test
        @DisplayName("Should return current user orders mapped to OrderResponse")
        void getMyOrders_WhenUserHasOrders_ShouldReturnMappedList() {
            customer.setOrders(List.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            List<OrderResponse> result = orderService.getMyOrders();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(501L, result.get(0).getId());

            verify(currentUserUtil).getCurrentUser();
            verify(modelMapper).map(sampleOrder, OrderResponse.class);
        }

        @Test
        @DisplayName("Should return empty list when current user has no orders")
        void getMyOrders_WhenUserHasNoOrders_ShouldReturnEmptyList() {
            customer.setOrders(Collections.emptyList());
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);

            List<OrderResponse> result = orderService.getMyOrders();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(currentUserUtil).getCurrentUser();
            verifyNoInteractions(modelMapper);
        }
    }

    // ==========================================
    // 5. updateOrderStatus Tests
    // ==========================================
    @Nested
    @DisplayName("updateOrderStatus Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should successfully update order status when seller is authorized")
        void updateOrderStatus_WhenAuthorizedSeller_ShouldUpdateStatusAndReturnResponse() {
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(seller1);
            when(orderRepository.save(sampleOrder)).thenReturn(sampleOrder);
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.DELIVERED);
            OrderResponse result = orderService.updateOrderStatus(501L, request);

            assertNotNull(result);
            assertEquals(OrderStatus.DELIVERED, sampleOrder.getOrderStatus());

            verify(orderRepository).findById(501L);
            verify(currentUserUtil).getCurrentUser();
            verify(orderRepository).save(sampleOrder);
            verify(modelMapper).map(sampleOrder, OrderResponse.class);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order does not exist")
        void updateOrderStatus_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.SHIPPED);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.updateOrderStatus(999L, request)
            );

            assertEquals("Order not found with ID: 999", exception.getMessage());
            verify(orderRepository).findById(999L);
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when current user is not the order's seller")
        void updateOrderStatus_WhenNotOrderSeller_ShouldThrowAuthorizationDeniedException() {
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(seller2); // seller2 != seller1

            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.SHIPPED);

            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> orderService.updateOrderStatus(501L, request)
            );

            assertEquals("You can only manage orders assigned to your seller account", exception.getMessage());
            verify(orderRepository).findById(501L);
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when updating to SHIPPED and payment is FAILED")
        void updateOrderStatus_WhenShippedAndPaymentFailed_ShouldThrowBadRequestException() {
            sampleOrder.setPaymentStatus(PaymentStatus.FAILED);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(seller1);

            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.SHIPPED);

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.updateOrderStatus(501L, request)
            );

            assertThat(exception.getMessage())
                    .contains("Cannot ship an order with payment status FAILED and order status SHIPPED");
            verify(orderRepository, never()).save(any());
        }
    }

    // ==========================================
    // 6. updatePaymentStatus Tests
    // ==========================================
    @Nested
    @DisplayName("updatePaymentStatus Tests")
    class UpdatePaymentStatusTests {

        @Test
        @DisplayName("Should transition order status to PLACED when payment status is updated to PAID and order was PENDING")
        void updatePaymentStatus_WhenPaidAndPending_ShouldUpdateOrderStatusToPlaced() {
            sampleOrder.setOrderStatus(OrderStatus.PENDING);
            sampleOrder.setPaymentStatus(PaymentStatus.PENDING);

            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(seller1);
            when(orderRepository.save(sampleOrder)).thenReturn(sampleOrder);
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest(PaymentStatus.PAID);
            OrderResponse result = orderService.updatePaymentStatus(501L, request);

            assertNotNull(result);
            assertEquals(OrderStatus.PLACED, sampleOrder.getOrderStatus());
            assertEquals(PaymentStatus.PAID, sampleOrder.getPaymentStatus());

            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("Should not change order status when payment status is updated to PAID but order was already SHIPPED")
        void updatePaymentStatus_WhenPaidAndAlreadyShipped_ShouldKeepExistingOrderStatus() {
            sampleOrder.setOrderStatus(OrderStatus.SHIPPED);
            sampleOrder.setPaymentStatus(PaymentStatus.PENDING);

            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(seller1);
            when(orderRepository.save(sampleOrder)).thenReturn(sampleOrder);
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest(PaymentStatus.PAID);
            orderService.updatePaymentStatus(501L, request);

            assertEquals(OrderStatus.SHIPPED, sampleOrder.getOrderStatus());
            assertEquals(PaymentStatus.PAID, sampleOrder.getPaymentStatus());
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("Should update payment status without changing order status when updated to FAILED")
        void updatePaymentStatus_WhenUpdatedToFailed_ShouldNotChangeOrderStatus() {
            sampleOrder.setOrderStatus(OrderStatus.PENDING);

            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(seller1);
            when(orderRepository.save(sampleOrder)).thenReturn(sampleOrder);
            when(modelMapper.map(sampleOrder, OrderResponse.class)).thenReturn(sampleOrderResponse);

            PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest(PaymentStatus.FAILED);
            orderService.updatePaymentStatus(501L, request);

            assertEquals(OrderStatus.PENDING, sampleOrder.getOrderStatus());
            assertEquals(PaymentStatus.FAILED, sampleOrder.getPaymentStatus());
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order does not exist")
        void updatePaymentStatus_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest(PaymentStatus.PAID);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.updatePaymentStatus(999L, request)
            );

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when current user is not the order's seller")
        void updatePaymentStatus_WhenNotOrderSeller_ShouldThrowAuthorizationDeniedException() {
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(currentUserUtil.getCurrentUser()).thenReturn(seller2);

            PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest(PaymentStatus.PAID);

            assertThrows(
                    AuthorizationDeniedException.class,
                    () -> orderService.updatePaymentStatus(501L, request)
            );

            verify(orderRepository, never()).save(any());
        }
    }

    // ==========================================
    // 7. placeOrder Tests
    // ==========================================
    @Nested
    @DisplayName("placeOrder Tests")
    class PlaceOrderTests {

        @Test
        @DisplayName("Should place order successfully with CASH_ON_DELIVERY and custom address")
        void placeOrder_Success_CashOnDelivery_WithCustomAddress() {
            customer.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderResponse.class))).thenReturn(sampleOrderResponse);

            OrderRequest request = OrderRequest.builder()
                    .productId(101L)
                    .quantity(2)
                    .address("Custom Shipping Address 789")
                    .build();

            OrderResponse response = orderService.placeOrder(request);

            assertNotNull(response);
            assertEquals(8, product1.getStockQuantity()); // 10 - 2
            verify(productRepository).save(product1);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(customer, savedOrder.getUser());
            assertEquals(seller1, savedOrder.getSeller());
            assertEquals("Custom Shipping Address 789", savedOrder.getAddress());
            assertEquals(new BigDecimal("100.00"), savedOrder.getTotalAmount());
            assertEquals(OrderStatus.PENDING, savedOrder.getOrderStatus());
            assertEquals(PaymentStatus.PENDING, savedOrder.getPaymentStatus());
            assertEquals(PaymentMethod.CASH_ON_DELIVERY, savedOrder.getPaymentMethod());
            assertEquals(1, savedOrder.getOrderItems().size());
            assertEquals(2, savedOrder.getOrderItems().get(0).getQuantity());
            assertEquals(product1.getPrice(), savedOrder.getOrderItems().get(0).getPriceAtPurchase());

            verifyNoInteractions(digitalBankingClientService);
        }

        @Test
        @DisplayName("Should fallback to user default address when request address is null")
        void placeOrder_Success_CashOnDelivery_WithUserDefaultAddress() {
            customer.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderResponse.class))).thenReturn(sampleOrderResponse);

            OrderRequest request = OrderRequest.builder()
                    .productId(101L)
                    .quantity(1)
                    .address(null)
                    .build();

            orderService.placeOrder(request);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(customer.getAddress(), savedOrder.getAddress());
        }

        @Test
        @DisplayName("Should place order successfully with BANK_TRANSFER and debit bank account")
        void placeOrder_Success_BankTransfer() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

            AccountResponse account = new AccountResponse(
                    1L,
                    "ACC-12345",
                    new BigDecimal("500.00"),
                    "ACTIVE",
                    "SAVINGS",
                    LocalDateTime.now()
            );
            when(digitalBankingClientService.getMyAccounts()).thenReturn(List.of(account));
            when(digitalBankingClientService.getAccountByNumber("ACC-12345")).thenReturn(account);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderResponse.class))).thenReturn(sampleOrderResponse);

            OrderRequest request = OrderRequest.builder()
                    .productId(101L)
                    .quantity(2) // 2 * 50.00 = 100.00
                    .accountNumber("ACC-12345")
                    .build();

            OrderResponse response = orderService.placeOrder(request);

            assertNotNull(response);

            ArgumentCaptor<TransactionRequest> withdrawCaptor = ArgumentCaptor.forClass(TransactionRequest.class);
            verify(digitalBankingClientService).withdraw(withdrawCaptor.capture());

            assertEquals("ACC-12345", withdrawCaptor.getValue().getAccountNumber());
            assertEquals(new BigDecimal("100.00"), withdrawCaptor.getValue().getAmount());

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(PaymentStatus.PAID, savedOrder.getPaymentStatus());
            assertEquals(PaymentMethod.BANK_TRANSFER, savedOrder.getPaymentMethod());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product is not found")
        void placeOrder_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            OrderRequest request = OrderRequest.builder()
                    .productId(999L)
                    .quantity(1)
                    .build();

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.placeOrder(request)
            );

            assertEquals("Product not found with ID: 999", exception.getMessage());
            verify(productRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when requested quantity exceeds product stock")
        void placeOrder_WhenStockInsufficient_ShouldThrowBadRequestException() {
            product1.setStockQuantity(2);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

            OrderRequest request = OrderRequest.builder()
                    .productId(101L)
                    .quantity(5)
                    .build();

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.placeOrder(request)
            );

            assertThat(exception.getMessage()).contains("Insufficient stock for product: " + product1.getName());
            verify(productRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when BANK_TRANSFER has no active accounts")
        void placeOrder_BankTransfer_WhenNoBankAccounts_ShouldThrowResourceNotFoundException() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
            when(digitalBankingClientService.getMyAccounts()).thenReturn(Collections.emptyList());

            OrderRequest request = OrderRequest.builder()
                    .productId(101L)
                    .quantity(1)
                    .accountNumber("ACC-12345")
                    .build();

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.placeOrder(request)
            );

            assertEquals("No active bank account for the user", exception.getMessage());
            verify(digitalBankingClientService, never()).withdraw(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when BANK_TRANSFER accountNumber is null")
        void placeOrder_BankTransfer_WhenAccountNumberNull_ShouldThrowResourceNotFoundException() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

            AccountResponse account = new AccountResponse(1L, "ACC-1", new BigDecimal("500.00"), "ACTIVE", "SAVINGS", LocalDateTime.now());
            when(digitalBankingClientService.getMyAccounts()).thenReturn(List.of(account));

            OrderRequest request = OrderRequest.builder()
                    .productId(101L)
                    .quantity(1)
                    .accountNumber(null)
                    .build();

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.placeOrder(request)
            );

            assertEquals("No account number found", exception.getMessage());
            verify(digitalBankingClientService, never()).withdraw(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when BANK_TRANSFER account balance is insufficient")
        void placeOrder_BankTransfer_WhenInsufficientBalance_ShouldThrowBadRequestException() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.findById(101L)).thenReturn(Optional.of(product1));

            AccountResponse account = new AccountResponse(1L, "ACC-12345", new BigDecimal("20.00"), "ACTIVE", "SAVINGS", LocalDateTime.now());
            when(digitalBankingClientService.getMyAccounts()).thenReturn(List.of(account));
            when(digitalBankingClientService.getAccountByNumber("ACC-12345")).thenReturn(account);

            OrderRequest request = OrderRequest.builder()
                    .productId(101L)
                    .quantity(1) // totalAmount = 50.00, balance = 20.00
                    .accountNumber("ACC-12345")
                    .build();

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.placeOrder(request)
            );

            assertEquals("Insufficient balance in the account", exception.getMessage());
            verify(digitalBankingClientService, never()).withdraw(any());
            verify(orderRepository, never()).save(any());
        }
    }

    // ==========================================
    // 8. placeOrderFromCart Tests
    // ==========================================
    @Nested
    @DisplayName("placeOrderFromCart Tests")
    class PlaceOrderFromCartTests {

        @Test
        @DisplayName("Should throw BadRequestException when user cart is null")
        void placeOrderFromCart_WhenCartIsNull_ShouldThrowBadRequestException() {
            customer.setCart(null);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.placeOrderFromCart()
            );

            assertEquals("Order cannot be placed with an empty cart", exception.getMessage());
            verifyNoInteractions(cartRepository);
            verifyNoInteractions(orderRepository);
        }

        @Test
        @DisplayName("Should throw BadRequestException when user cart has no items")
        void placeOrderFromCart_WhenCartIsEmpty_ShouldThrowBadRequestException() {
            Cart emptyCart = Cart.builder().cartItems(new ArrayList<>()).build();
            customer.setCart(emptyCart);
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.placeOrderFromCart()
            );

            assertEquals("Order cannot be placed with an empty cart", exception.getMessage());
            verifyNoInteractions(cartRepository);
            verifyNoInteractions(orderRepository);
        }

        @Test
        @DisplayName("Should throw BadRequestException when any item in cart exceeds product stock")
        void placeOrderFromCart_WhenItemStockInsufficient_ShouldThrowBadRequestException() {
            product1.setStockQuantity(2);
            CartItem cartItem = CartItem.builder().product(product1).quantity(5).build();
            Cart cart = Cart.builder().cartItems(new ArrayList<>(List.of(cartItem))).build();
            customer.setCart(cart);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.placeOrderFromCart()
            );

            assertThat(exception.getMessage()).contains("Insufficient stock for product: " + product1.getName());
            verify(productRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should place order from cart successfully with CASH_ON_DELIVERY for single seller")
        void placeOrderFromCart_Success_CashOnDelivery_SingleSeller() {
            customer.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            CartItem item = CartItem.builder().product(product1).quantity(2).build();
            Cart cart = Cart.builder().cartItems(new ArrayList<>(List.of(item))).build();
            customer.setCart(cart);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderResponse.class))).thenReturn(sampleOrderResponse);

            List<OrderResponse> result = orderService.placeOrderFromCart();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(8, product1.getStockQuantity()); // 10 - 2
            assertTrue(cart.getCartItems().isEmpty());

            verify(productRepository).save(product1);
            verify(cartRepository).save(cart);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(customer, savedOrder.getUser());
            assertEquals(seller1, savedOrder.getSeller());
            assertEquals(new BigDecimal("100.00"), savedOrder.getTotalAmount());
            assertEquals(OrderStatus.PENDING, savedOrder.getOrderStatus());
            assertEquals(PaymentStatus.PENDING, savedOrder.getPaymentStatus());
            assertEquals(PaymentMethod.CASH_ON_DELIVERY, savedOrder.getPaymentMethod());
        }

        @Test
        @DisplayName("Should create multiple separate orders grouped by seller when cart has items from multiple sellers")
        void placeOrderFromCart_Success_CashOnDelivery_MultipleSellers() {
            customer.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            CartItem item1 = CartItem.builder().product(product1).quantity(2).build(); // 2 * 50 = 100 (seller1)
            CartItem item2 = CartItem.builder().product(product2).quantity(1).build(); // 1 * 120 = 120 (seller2)
            Cart cart = Cart.builder().cartItems(new ArrayList<>(List.of(item1, item2))).build();
            customer.setCart(cart);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderResponse.class))).thenReturn(sampleOrderResponse);

            List<OrderResponse> result = orderService.placeOrderFromCart();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(8, product1.getStockQuantity());
            assertEquals(4, product2.getStockQuantity());
            assertTrue(cart.getCartItems().isEmpty());

            verify(cartRepository).save(cart);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(2)).save(orderCaptor.capture());

            List<Order> savedOrders = orderCaptor.getAllValues();
            Order orderForSeller1 = savedOrders.stream()
                    .filter(o -> o.getSeller().equals(seller1))
                    .findFirst()
                    .orElseThrow();
            Order orderForSeller2 = savedOrders.stream()
                    .filter(o -> o.getSeller().equals(seller2))
                    .findFirst()
                    .orElseThrow();

            assertEquals(new BigDecimal("100.00"), orderForSeller1.getTotalAmount());
            assertEquals(new BigDecimal("120.00"), orderForSeller2.getTotalAmount());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when BANK_TRANSFER has no active accounts in placeOrderFromCart")
        void placeOrderFromCart_BankTransfer_WhenNoAccounts_ShouldThrowResourceNotFoundException() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            CartItem item = CartItem.builder().product(product1).quantity(1).build();
            Cart cart = Cart.builder().cartItems(new ArrayList<>(List.of(item))).build();
            customer.setCart(cart);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(digitalBankingClientService.getMyAccounts()).thenReturn(Collections.emptyList());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.placeOrderFromCart()
            );

            assertEquals("No active bank account found for user", exception.getMessage());
            verify(digitalBankingClientService, never()).withdraw(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when BANK_TRANSFER primary account balance is insufficient")
        void placeOrderFromCart_BankTransfer_WhenInsufficientBalance_ShouldThrowBadRequestException() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            CartItem item = CartItem.builder().product(product1).quantity(2).build(); // 100.00 total
            Cart cart = Cart.builder().cartItems(new ArrayList<>(List.of(item))).build();
            customer.setCart(cart);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);

            AccountResponse lowBalanceAccount = new AccountResponse(
                    1L,
                    "ACC-PRIMARY",
                    new BigDecimal("50.00"),
                    "ACTIVE",
                    "SAVINGS",
                    LocalDateTime.now()
            );
            when(digitalBankingClientService.getMyAccounts()).thenReturn(List.of(lowBalanceAccount));

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.placeOrderFromCart()
            );

            assertEquals("Insufficient balance in account", exception.getMessage());
            verify(digitalBankingClientService, never()).withdraw(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should withdraw total across all sellers and mark orders PAID when BANK_TRANSFER")
        void placeOrderFromCart_Success_BankTransfer() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            CartItem item1 = CartItem.builder().product(product1).quantity(2).build(); // 100.00
            CartItem item2 = CartItem.builder().product(product2).quantity(1).build(); // 120.00
            Cart cart = Cart.builder().cartItems(new ArrayList<>(List.of(item1, item2))).build();
            customer.setCart(cart);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);

            AccountResponse primaryAccount = new AccountResponse(
                    1L,
                    "ACC-PRIMARY",
                    new BigDecimal("500.00"),
                    "ACTIVE",
                    "SAVINGS",
                    LocalDateTime.now()
            );
            when(digitalBankingClientService.getMyAccounts()).thenReturn(List.of(primaryAccount));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderResponse.class))).thenReturn(sampleOrderResponse);

            orderService.placeOrderFromCart();

            ArgumentCaptor<TransactionRequest> withdrawCaptor = ArgumentCaptor.forClass(TransactionRequest.class);
            verify(digitalBankingClientService).withdraw(withdrawCaptor.capture());

            assertEquals("ACC-PRIMARY", withdrawCaptor.getValue().getAccountNumber());
            assertEquals(new BigDecimal("220.00"), withdrawCaptor.getValue().getAmount()); // 100 + 120

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(2)).save(orderCaptor.capture());

            for (Order o : orderCaptor.getAllValues()) {
                assertEquals(PaymentStatus.PAID, o.getPaymentStatus());
                assertEquals(PaymentMethod.BANK_TRANSFER, o.getPaymentMethod());
            }
        }
    }

    // ==========================================
    // 9. cancelOrder Tests
    // ==========================================
    @Nested
    @DisplayName("cancelOrder Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order to cancel is not found")
        void cancelOrder_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.cancelOrder(999L)
            );

            assertEquals("Order not found with ID: 999", exception.getMessage());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when user tries to cancel another user's order")
        void cancelOrder_WhenNotOrderOwner_ShouldThrowAuthorizationDeniedException() {
            User otherUser = User.builder().id(888L).build();
            sampleOrder.setUser(otherUser);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));

            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> orderService.cancelOrder(501L)
            );

            assertEquals("You can only cancel your own orders", exception.getMessage());
            verify(orderRepository, never()).save(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when trying to cancel an already DELIVERED order")
        void cancelOrder_WhenAlreadyDelivered_ShouldThrowBadRequestException() {
            sampleOrder.setOrderStatus(OrderStatus.DELIVERED);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.cancelOrder(501L)
            );

            assertThat(exception.getMessage()).contains("Cannot cancel an order with order status: DELIVERED");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when trying to cancel an already CANCELLED order")
        void cancelOrder_WhenAlreadyCancelled_ShouldThrowBadRequestException() {
            sampleOrder.setOrderStatus(OrderStatus.CANCELLED);

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));

            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> orderService.cancelOrder(501L)
            );

            assertThat(exception.getMessage()).contains("Cannot cancel an order with order status: CANCELLED");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should cancel CASH_ON_DELIVERY order, restore stock, and update status without refund call")
        void cancelOrder_Success_CashOnDelivery() {
            customer.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            sampleOrder.setOrderStatus(OrderStatus.PLACED);
            sampleOrder.setPaymentStatus(PaymentStatus.PENDING);

            product1.setStockQuantity(5);
            OrderItem item1 = OrderItem.builder().order(sampleOrder).product(product1).quantity(3).build();

            product2.setStockQuantity(2);
            OrderItem item2 = OrderItem.builder().order(sampleOrder).product(product2).quantity(2).build();

            sampleOrder.setOrderItems(new ArrayList<>(List.of(item1, item2)));

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.cancelOrder(501L);

            // Verify stock restored
            assertEquals(8, product1.getStockQuantity()); // 5 + 3
            assertEquals(4, product2.getStockQuantity()); // 2 + 2
            verify(productRepository).save(product1);
            verify(productRepository).save(product2);

            // No digital banking call
            verifyNoInteractions(digitalBankingClientService);

            // Verify order status updated and saved
            assertEquals(OrderStatus.CANCELLED, sampleOrder.getOrderStatus());
            assertEquals(PaymentStatus.REFUNDED, sampleOrder.getPaymentStatus());
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when BANK_TRANSFER paid order is cancelled but user has no accounts for refund")
        void cancelOrder_BankTransferPaid_WhenNoAccounts_ShouldThrowResourceNotFoundException() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            sampleOrder.setOrderStatus(OrderStatus.PLACED);
            sampleOrder.setPaymentStatus(PaymentStatus.PAID);

            OrderItem item = OrderItem.builder().order(sampleOrder).product(product1).quantity(1).build();
            sampleOrder.setOrderItems(new ArrayList<>(List.of(item)));

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(digitalBankingClientService.getMyAccounts()).thenReturn(Collections.emptyList());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.cancelOrder(501L)
            );

            assertEquals("No active bank account found for user", exception.getMessage());
            verify(digitalBankingClientService, never()).deposit(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should cancel BANK_TRANSFER paid order, restore stock, deposit refund, and update status")
        void cancelOrder_Success_BankTransferPaid() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            sampleOrder.setOrderStatus(OrderStatus.PLACED);
            sampleOrder.setPaymentStatus(PaymentStatus.PAID);
            sampleOrder.setTotalAmount(new BigDecimal("150.00"));

            product1.setStockQuantity(5);
            OrderItem item = OrderItem.builder().order(sampleOrder).product(product1).quantity(2).build();
            sampleOrder.setOrderItems(new ArrayList<>(List.of(item)));

            AccountResponse primaryAccount = new AccountResponse(
                    1L,
                    "REFUND-ACC-777",
                    new BigDecimal("1000.00"),
                    "ACTIVE",
                    "SAVINGS",
                    LocalDateTime.now()
            );

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(digitalBankingClientService.getMyAccounts()).thenReturn(List.of(primaryAccount));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.cancelOrder(501L);

            // Verify stock restored
            assertEquals(7, product1.getStockQuantity());
            verify(productRepository).save(product1);

            // Verify deposit refund called
            ArgumentCaptor<TransactionRequest> depositCaptor = ArgumentCaptor.forClass(TransactionRequest.class);
            verify(digitalBankingClientService).deposit(depositCaptor.capture());

            assertEquals("REFUND-ACC-777", depositCaptor.getValue().getAccountNumber());
            assertEquals(new BigDecimal("150.00"), depositCaptor.getValue().getAmount());

            // Verify order status
            assertEquals(OrderStatus.CANCELLED, sampleOrder.getOrderStatus());
            assertEquals(PaymentStatus.REFUNDED, sampleOrder.getPaymentStatus());
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("Should not deposit refund if BANK_TRANSFER order paymentStatus was PENDING")
        void cancelOrder_Success_BankTransfer_WhenPaymentWasPending() {
            customer.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            sampleOrder.setOrderStatus(OrderStatus.PENDING);
            sampleOrder.setPaymentStatus(PaymentStatus.PENDING); // Not PAID

            OrderItem item = OrderItem.builder().order(sampleOrder).product(product1).quantity(1).build();
            sampleOrder.setOrderItems(new ArrayList<>(List.of(item)));

            when(currentUserUtil.getCurrentUser()).thenReturn(customer);
            when(orderRepository.findById(501L)).thenReturn(Optional.of(sampleOrder));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.cancelOrder(501L);

            verify(digitalBankingClientService, never()).deposit(any());
            assertEquals(OrderStatus.CANCELLED, sampleOrder.getOrderStatus());
            assertEquals(PaymentStatus.REFUNDED, sampleOrder.getPaymentStatus());
            verify(orderRepository).save(sampleOrder);
        }
    }
}
