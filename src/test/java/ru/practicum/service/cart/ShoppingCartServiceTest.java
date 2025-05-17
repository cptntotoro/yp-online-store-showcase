//package ru.practicum.service.cart;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import ru.practicum.model.cart.CartItem;
//import ru.practicum.model.product.Product;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class ShoppingCartServiceTest {
//
//    private ShoppingCartService cartService;
//    private Product product1;
//    private Product product2;
//
//    @BeforeEach
//    void setUp() {
//        cartService = new ShoppingCartServiceImpl();
//
//        product1 = new Product();
//        product1.setUuid(UUID.randomUUID());
//        product1.setName("Product 1");
//        product1.setPrice(BigDecimal.valueOf(100.0));
//        product1.setImageUrl("/images/product1.jpg");
//
//        product2 = new Product();
//        product2.setUuid(UUID.randomUUID());
//        product2.setName("Product 2");
//        product2.setPrice(BigDecimal.valueOf(200.0));
//        product2.setImageUrl("/images/product2.jpg");
//    }
//
//    @Test
//    void add_shouldAddNewItemToEmptyCart() {
//        cartService.addToCart(product1, 2);
//
//        List<CartItem> items = cartService.getAll();
//        assertEquals(1, items.size());
//        assertEquals(product1.getUuid(), items.getFirst().getProductUuid());
//        assertEquals(2, items.getFirst().getQuantity());
//    }
//
//    @Test
//    void add_shouldIncreaseQuantityForExistingItem() {
//        cartService.addToCart(product1, 2);
//        cartService.addToCart(product1, 3);
//
//        List<CartItem> items = cartService.getAll();
//        assertEquals(1, items.size());
//        assertEquals(5, items.getFirst().getQuantity());
//    }
//
//    @Test
//    void remove_shouldRemoveItemFromCart() {
//        cartService.addToCart(product1, 1);
//        cartService.addToCart(product2, 1);
//
//        cartService.remove(product1.getUuid());
//
//        List<CartItem> items = cartService.getAll();
//        assertEquals(1, items.size());
//        assertEquals(product2.getUuid(), items.getFirst().getProductUuid());
//    }
//
//    @Test
//    void updateQuantity_shouldUpdateExistingItemQuantity() {
//        cartService.addToCart(product1, 1);
//        cartService.updateQuantity(product1.getUuid(), 5);
//
//        assertEquals(5, cartService.getAll().getFirst().getQuantity());
//    }
//
//    @Test
//    void updateQuantity_shouldRemoveItemWhenQuantityZeroOrNegative() {
//        cartService.addToCart(product1, 1);
//        cartService.updateQuantity(product1.getUuid(), 0);
//
//        assertTrue(cartService.getAll().isEmpty());
//    }
//
//    @Test
//    void getAll_shouldReturnAllItemsInCart() {
//        cartService.addToCart(product1, 1);
//        cartService.addToCart(product2, 2);
//
//        List<CartItem> items = cartService.getAll();
//        assertEquals(2, items.size());
//        assertTrue(items.stream().anyMatch(i -> i.getProductUuid().equals(product1.getUuid())));
//        assertTrue(items.stream().anyMatch(i -> i.getProductUuid().equals(product2.getUuid())));
//    }
//
//    @Test
//    void clear_shouldRemoveAllItemsFromCart() {
//        cartService.addToCart(product1, 1);
//        cartService.addToCart(product2, 1);
//
//        cartService.clear();
//
//        assertTrue(cartService.getAll().isEmpty());
//    }
//
//    @Test
//    void getTotalItems_shouldReturnSumOfAllQuantities() {
//        cartService.addToCart(product1, 2);
//        cartService.addToCart(product2, 3);
//
//        assertEquals(5, cartService.getTotalItems());
//    }
//
//    @Test
//    void getTotalPrice_shouldReturnCorrectTotal() {
//        cartService.addToCart(product1, 2); // 2 x 100 = 200
//        cartService.addToCart(product2, 1); // 1 x 200 = 200
//
//        assertEquals(BigDecimal.valueOf(400.0), cartService.getTotalPrice());
//    }
//
//    @Test
//    void getTotalPrice_shouldReturnZeroForEmptyCart() {
//        assertEquals(BigDecimal.valueOf(0.0), cartService.getTotalPrice());
//    }
//
//    @Test
//    void sessionScoped_shouldMaintainSeparateCarts() {
//        ShoppingCartServiceImpl anotherCartService = new ShoppingCartServiceImpl();
//
//        cartService.addToCart(product1, 1);
//        anotherCartService.addToCart(product2, 1);
//
//        assertEquals(1, cartService.getAll().size());
//        assertEquals(1, anotherCartService.getAll().size());
//        assertNotEquals(cartService.getAll(), anotherCartService.getAll());
//    }
//}