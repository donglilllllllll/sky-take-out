package com.sky.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 凯祥
 * @description 针对表【shopping_cart(购物车)】的数据库操作Service实现
 * @createDate 2024-08-14 17:14:57
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车添加记录开始======");
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //获取当前登录人ID
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);//用户ID
        //判断当前加入的购物车记录是否在购物车中
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, currentId)
                .eq(shoppingCart.getDishId() != null && shoppingCart.getDishId() != 0L, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(shoppingCart.getSetmealId() != null && shoppingCart.getSetmealId() != 0L, ShoppingCart::getSetmealId, shoppingCart.getSetmealId())
                .eq(StringUtils.isNotBlank(shoppingCart.getDishFlavor()), ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor())
        );
        //如果已存在，只需要数量加1
        if (shoppingCarts != null && !shoppingCarts.isEmpty()) {
            log.info("购物车中已存在该记录，数量加1");
            ShoppingCart cart = shoppingCarts.get(0);
            shoppingCart.setNumber(cart.getNumber() + 1);
            shoppingCart.setId(cart.getId());
            shoppingCartMapper.updateById(shoppingCart);
        } else {
            //如果不存在，需要新增购物车记录
            log.info("购物车中不存在该记录，新增记录");
            if (shoppingCart.getDishId() != null) {
                //菜品
                log.info("新增的菜品");
                Dish dish = dishMapper.selectById(shoppingCart.getDishId());
                shoppingCart.setName(dish.getName());//菜品名称
                shoppingCart.setImage(dish.getImage());//菜品图片地址
                shoppingCart.setAmount(dish.getPrice());//菜品价格
            } else {
                //套餐
                log.info("新增的套餐");
                Setmeal setmeal = setmealMapper.selectById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);//菜品数量
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        log.info("查询购物车记录开始======");
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .orderByAsc(ShoppingCart::getCreateTime)
        );
    }

    @Override
    public void clean() {
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId()));
    }

    @Override
    public void deleteShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车添加记录开始======");
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //获取当前登录人ID
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询这个购物车记录
        ShoppingCart cart = shoppingCartMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                .eq(shoppingCart.getDishId() != null && shoppingCart.getDishId() != 0L, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(StringUtils.isNotBlank(shoppingCart.getDishFlavor()), ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor())
                .eq(shoppingCart.getSetmealId() != null && shoppingCart.getSetmealId() != 0L, ShoppingCart::getSetmealId, shoppingCart.getSetmealId())
        );
        //如果存在，更新number
        if (cart != null) {
            if (cart.getNumber() == 1) {
                //如果number=1，删除该购物车记录
                shoppingCartMapper.deleteById(cart.getId());
            } else {
                //如果 number>1，更新number-1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateById(cart);
            }
        }


    }
}




