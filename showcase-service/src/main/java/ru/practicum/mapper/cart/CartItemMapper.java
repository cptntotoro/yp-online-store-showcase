package ru.practicum.mapper.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.cart.CartItemDao;
import ru.practicum.dto.cart.cache.CartItemCacheDto;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.cart.CartItem;

/**
 * Маппер товаров корзины
 */
@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface CartItemMapper {

    /**
     * Смаппить товар корзины в DTO товара корзины
     *
     * @param cartItemDto Товар корзины
     * @return DTO товара корзины
     */
    @Mapping(target = "product", source = "product")
    CartItemDto cartItemToCartItemDto(CartItem cartItemDto);

    /**
     * Смаппить товар корзины в DAO товара корзины
     *
     * @param cartItem Товар корзины
     * @return DAO товара корзины
     */
    @Mapping(target = "productUuid", source = "product.uuid")
    CartItemDao cartItemToCartItemDao(CartItem cartItem);

    /**
     * Смаппить DAO товара корзины в товар корзины
     *
     * @param cartItemDao DAO товара корзины
     * @return Товар корзины
     */
    @Mapping(target = "uuid", source = "cartItemDao.uuid")
    @Mapping(target = "cartUuid", source = "cartItemDao.cartUuid")
    @Mapping(target = "quantity", source = "cartItemDao.quantity")
    @Mapping(target = "createdAt", source = "cartItemDao.createdAt")
    @Mapping(target = "product", expression = "java(cartItemDao.getProductUuid() != null ? Product.builder().uuid(cartItemDao.getProductUuid()).build() : null)")
    CartItem cartItemDaoToCartItem(CartItemDao cartItemDao);

    /**
     * Смаппить товар корзины в DTO товара корзины для кеша
     *
     * @param cartItem Товар корзины
     * @return DTO товара корзины для кеша
     */
    @Mapping(target = "productUuid", expression = "java(cartItem.getProduct() != null ? cartItem.getProduct().getUuid() : null)")
    CartItemCacheDto cartItemToCartItemCacheDto(CartItem cartItem);

    /**
     * Смаппить DTO товара корзины для кеша в товар корзины
     *
     * @param cartItemCacheDto DTO товара корзины для кеша
     * @return Товар корзины
     */
    @Mapping(target = "product", expression = "java(cartItemCacheDto.getProductUuid() != null ? Product.builder().uuid(cartItemCacheDto.getProductUuid()).build() : null)")
    @Mapping(target = "createdAt", ignore = true)
    CartItem cartItemCacheDtoToCartItem(CartItemCacheDto cartItemCacheDto);
}
