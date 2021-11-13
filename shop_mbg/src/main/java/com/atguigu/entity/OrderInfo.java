package com.atguigu.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单表 订单表
 * </p>
 *
 * @author xiejl
 * @since 2021-11-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_info")
@ApiModel(value="OrderInfo对象", description="订单表 订单表")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "收货人")
    private String consignee;

    @ApiModelProperty(value = "收件人电话")
    private String consigneeTel;

    @ApiModelProperty(value = "总金额")
    private BigDecimal totalMoney;

    @ApiModelProperty(value = "订单状态")
    private String orderStatus;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "付款方式")
    private String paymentWay;

    @ApiModelProperty(value = "送货地址")
    private String deliveryAddress;

    @ApiModelProperty(value = "订单备注")
    private String orderComment;

    @ApiModelProperty(value = "订单交易编号（第三方支付用)")
    private String outTradeNo;

    @ApiModelProperty(value = "订单描述(第三方支付用)")
    private String tradeBody;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "失效时间")
    private Date expireTime;

    @ApiModelProperty(value = "进度状态")
    private String processStatus;

    @ApiModelProperty(value = "父订单编号")
    private Long parentOrderId;

    @ApiModelProperty(value = "图片路径")
    private String imgUrl;

    @ApiModelProperty(value = "运单编号")
    private String logisticsNum;


}
