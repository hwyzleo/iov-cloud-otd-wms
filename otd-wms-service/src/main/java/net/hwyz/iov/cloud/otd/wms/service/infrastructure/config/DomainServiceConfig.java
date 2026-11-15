package net.hwyz.iov.cloud.otd.wms.service.infrastructure.config;

import net.hwyz.iov.cloud.otd.wms.service.domain.repository.WarehouseMasterRepository;
import net.hwyz.iov.cloud.otd.wms.service.domain.service.StorageHierarchyValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务装配
 * <p>
 * 领域层保持框架无关，领域服务在此以 Bean 形式装配，仓储端口由基础设施实现注入。
 *
 * @author hwyz_leo
 */
@Configuration
public class DomainServiceConfig {

    /**
     * 仓储主数据层级校验领域服务
     *
     * @param warehouseMasterRepository 仓储主数据仓储端口
     * @return 领域服务
     */
    @Bean
    public StorageHierarchyValidator storageHierarchyValidator(WarehouseMasterRepository warehouseMasterRepository) {
        return new StorageHierarchyValidator(warehouseMasterRepository);
    }
}
