package com.zoowii.jpa_utils.extension;

import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.Finder;
import com.zoowii.jpa_utils.query.Query;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 这是一个扩展的Finder类，如果用户有需要可以自行再继承来进行扩展
 * 推荐对查询复杂的Model类定义一个单独的Finder类，然后在里面增加各种查询方法
 * 比如对于Model类UserEntity，定义一个UserFinder类，在里面可以加上各种比如findByName等方法，方便使用，并避免过多修改model类本身
 * Created by zoowii on 14-12-23.
 */
public class ExtendFinder<K, M> extends Finder<K, M> {
    public ExtendFinder(Class<?> kCls, Class<?> mCls) {
        super(kCls, mCls);
    }

    /**
     * 这是使用简单分页类的例子，如果有需要,可以自行再扩展
     *
     * @param paginator
     * @return
     */
    public List<M> findByPaginator(Paginator paginator) {
        if (paginator == null) {
            return this.findAll();
        }
        Query<M> query = this.where();
        for (Expr expr : paginator.getExpressions()) {
            query = query.and(expr);
        }
        for (Pair<String, Boolean> orderBy : paginator.getOrders()) {
            query = query.orderBy(orderBy.getLeft(), orderBy.getRight());
        }
        for (int i = 0; i < paginator.getParameters().size(); ++i) {
            query = query.setParameter(i + 1, paginator.getParameters().get(i));
        }
        if (paginator.getBeforeQueryProcessor() != null) {
            query = paginator.getBeforeQueryProcessor().apply(query);
        }
        paginator.setTotal(query.count());
        return query.offset(paginator.getIntSkippedCount()).limit(paginator.getIntPageSize()).all();
    }
}
