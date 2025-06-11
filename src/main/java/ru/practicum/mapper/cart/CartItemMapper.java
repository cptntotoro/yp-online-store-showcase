package ru.practicum.mapper.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.cart.CartItemDao;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.cart.CartItem;

/**
 * Маппер товаров корзины
 */
@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface CartItemMapper {

    /**
     * Смаппить товар из корзины в DTO товара из корзины
     *
     * @param cartItemDto Товар из корзины
     * @return DTO товара из корзины
     */
    @Mapping(target = "product", source = "product")
    CartItemDto cartItemToCartItemDto(CartItem cartItemDto);

    /**
     * Смаппить товар из корзины в DAO товара из корзины
     *
     * @param cartItem Товар из корзины
     * @return DAO товара из корзины
     */
    @Mapping(target = "productUuid", source = "product.uuid")
    CartItemDao cartItemToCartItemDao(CartItem cartItem);

    /**
     * Смаппить DAO товара из корзины в товар из корзины
     *
     * @param cartItemDao DAO товара из корзины
     * @return Товар из корзины
     */
    @Mapping(target = "uuid", source = "cartItemDao.uuid")
    @Mapping(target = "cartUuid", source = "cartItemDao.cartUuid")
    @Mapping(target = "quantity", source = "cartItemDao.quantity")
    @Mapping(target = "createdAt", source = "cartItemDao.createdAt")
    @Mapping(target = "product", ignore = true)
    CartItem cartItemDaoToCartItem(CartItemDao cartItemDao);
}
