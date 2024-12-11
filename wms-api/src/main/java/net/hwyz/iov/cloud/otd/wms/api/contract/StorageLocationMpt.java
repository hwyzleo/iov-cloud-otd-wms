package net.hwyz.iov.cloud.otd.wms.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.util.Date;

/**
 * 管理后台仓库储位
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StorageLocationMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 储位代码
     */
    private String code;

    /**
     * 储位名称
     */
    private String name;

    /**
     * 仓库代码
     */
    private String warehouseCode;

    /**
     * 储区代码
     */
    private String storageAreaCode;

    /**
     * 储位管理员
     */
    private String manager;

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
