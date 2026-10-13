package com.myProject.service;

import com.myProject.category.entity.Category;
import com.myProject.category.repository.CategoryRepository;
import com.myProject.product.dto.*;
import com.myProject.product.entity.*;
import com.myProject.product.repository.ProductRepository;
import com.myProject.product.service.ProductService;
import com.myProject.user.entity.*;
import com.myProject.security.user.CurrentUserUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CurrentUserUtil currentUserUtil;

    private User seller;
    private Category category;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(2L)
                .name("seller")
                .email("seller@gmail.com")
                .password("hashed_password_123456")
                .role(Role.ROLE_SELLER)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .address("Toronto, 342 dorito street")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Sports")
                .build();
    }

    @Test
    void getProductByIdTest() {
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        User seller = User.builder()
                .id(1L)
                .name("John Welling")
                .role(Role.ROLE_SELLER)
                .address("Eddy Castle, 16th floor, Remote street, Russia-290112")
                .password("hashed_password_123")
                .email("seller@gmail.com")
                .build();

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Samsung Television")
                .price(new BigDecimal("20000"))
                .description("Curved HD TV with free subscription for one month")
                .productImages(new ArrayList<>())
                .stockQuantity(15)
                .user(seller)
                .category(category)
                .build();

        ProductResponse expectedResponse = ProductResponse.builder()
                .id(1L)
                .name("Samsung Television")
                .price(new BigDecimal("20000"))
                .description("Curved HD TV with free subscription for one month")
                .stockQuantity(15)
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(expectedResponse);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        ProductResponse actualResponse = productService.getProductById(1L);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getName(), actualResponse.getName());
    }

    @Test
    void createProductTest() {
        ProductImage image = ProductImage.builder()
                .id(1L)
                .imageUrl("https://plus.unsplash.com/premium_photo-1679917506585-2c7b89054610?w=600&auto=format&fit=crop&q=60")
                .build();

        NewProductRequest newProductRequest = new NewProductRequest(
                "Cricket bat",
                "https://plus.unsplash.com/premium_photo-1679917506585-2c7b89054610?w=600&auto=format&fit=crop&q=60",
                new BigDecimal("1500"),
                "Durable test cricket bat",
                10,
                1L
        );

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(currentUserUtil.getCurrentUser()).thenReturn(seller);

        Product product = Product.builder()
                .id(1L)
                .name(newProductRequest.getName())
                .price(newProductRequest.getPrice())
                .description(newProductRequest.getDescription())
                .stockQuantity(newProductRequest.getStockQuantity())
                .productImages(new ArrayList<>())
                .build();
        image.setProduct(product);
        product.getProductImages().add(image);

        when(modelMapper.map(any(NewProductRequest.class), eq(Product.class))).thenReturn(product);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product argument = invocation.getArgument(0);
            argument.setId(100L); // Mimics database auto-increment ID generation
            return argument;
        });

        ProductResponse expectedResponse = ProductResponse.builder()
                .id(100L)
                .name("Cricket bat")
                .price(new BigDecimal("1500"))
                .stockQuantity(10)
                .categoryId(1L)
                .categoryName("Sports")
                .build();

        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(expectedResponse);

        ProductResponse actualResponse = productService.createNewProduct(newProductRequest);

        assertNotNull(actualResponse);
        assertEquals(100L, actualResponse.getId());
        assertEquals("Cricket bat", actualResponse.getName());
        assertEquals("Sports", actualResponse.getCategoryName());
    }

    @Test
    void updateProductTest() {
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        User seller = User.builder()
                .id(1L)
                .name("John Welling")
                .role(Role.ROLE_SELLER)
                .address("Eddy Castle, 16th floor, Remote street, Russia-290112")
                .password("hashed_password_123")
                .email("seller@gmail.com")
                .build();

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Samsung Television")
                .price(new BigDecimal("20000"))
                .description("Curved HD TV with free subscription for one month")
                .productImages(new ArrayList<>())
                .stockQuantity(15)
                .user(seller)
                .category(category)
                .build();

        UpdateProductRequest request = UpdateProductRequest.builder()
                .price(new BigDecimal("22000"))
                .stockQuantity(20)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(currentUserUtil.getCurrentUser()).thenReturn(seller);
        when(productRepository.save(any(Product.class))).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        ProductResponse expectedResponse = ProductResponse.builder()
                .id(1L)
                .name("Samsung Television")
                .price(new BigDecimal("22000"))
                .description("Curved HD TV with free subscription for one month")
                .stockQuantity(20)
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        when(modelMapper.map(any(Product.class), eq(ProductResponse.class))).thenReturn(expectedResponse);

        ProductResponse actualResponse = productService.updateProduct(1L, request);

        assertNotNull(actualResponse);
        assertEquals(new BigDecimal("22000"), actualResponse.getPrice());
        assertEquals(20, actualResponse.getStockQuantity());
        assertEquals("Samsung Television", actualResponse.getName());

    }
}