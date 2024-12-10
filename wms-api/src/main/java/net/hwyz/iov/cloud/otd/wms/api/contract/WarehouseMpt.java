package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.util.Date;

/**
 * 管理后台仓库
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WarehouseMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 仓库代码
     */
    private String code;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 仓库管理员
     */
    private String manager;

    /**
     * 仓库体系层级
     */
    private String warehouseLevel;

    /**
     * 省级行政区代码
     */
    private String provinceCode;

    /**
     * 地区级行政区代码
     */
    private String cityCode;

    /**
     * 县级行政区代码
     */
    private String countyCode;

    /**
     * 仓库地址
     */
    private String address;

    /**
     * 邮编
     */
    private String zipcode;

    /**
     * 传真
     */
    private String fax;

    /**
     * 电话
     */
    private String tel;

    /**
     * 手机
     */
    private String mobile;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

}
