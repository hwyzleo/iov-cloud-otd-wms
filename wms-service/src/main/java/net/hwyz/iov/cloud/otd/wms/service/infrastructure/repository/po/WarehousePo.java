package net.hwyz.iov.cloud.otd.wms.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 整车仓库 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-12-10
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_warehouse")
public class WarehousePo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 仓库代码
     */
    @TableField("code")
    private String code;

    /**
     * 仓库名称
     */
    @TableField("name")
    private String name;

    /**
     * 仓库管理员
     */
    @TableField("manager")
    private String manager;

    /**
     * 仓库体系层级
     */
    @TableField("warehouse_level")
    private String warehouseLevel;

    /**
     * 省级行政区代码
     */
    @TableField("province_code")
    private String provinceCode;

    /**
     * 地区级行政区代码
     */
    @TableField("city_code")
    private String cityCode;

    /**
     * 县级行政区代码
     */
    @TableField("county_code")
    private String countyCode;

    /**
     * 仓库地址
     */
    @TableField("address")
    private String address;

    /**
     * 邮编
     */
    @TableField("zipcode")
    private String zipcode;

    /**
     * 传真
     */
    @TableField("fax")
    private String fax;

    /**
     * 电话
     */
    @TableField("tel")
    private String tel;

    /**
     * 手机
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 是否启用
     */
    @TableField("enable")
    private Boolean enable;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;
}
