package net.hwyz.iov.cloud.otd.wms.service.adapter.web;

import com.github.pagehelper.PageInfo;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * 分页结果装配
 * <p>
 * 保留 MyBatis 物理分页元数据（PageHelper Page），转换元素类型后不丢失 total。
 *
 * @author hwyz_leo
 */
public final class PageResults {

    private PageResults() {
    }

    /**
     * 转换分页列表并保留分页元数据
     *
     * @param source 原始分页列表
     * @param mapper 元素转换函数
     * @param <S>    源类型
     * @param <T>    目标类型
     * @return 分页结果
     */
    public static <S, T> PageResult<T> of(List<S> source, Function<S, T> mapper) {
        PageInfo<S> info = new PageInfo<>(source);
        return PageResult.<T>builder()
                .items(source.stream().map(mapper).toList())
                .total(info.getTotal())
                .page(Math.max(info.getPageNum() - 1, 0))
                .size(info.getPageSize())
                .build();
    }
}
