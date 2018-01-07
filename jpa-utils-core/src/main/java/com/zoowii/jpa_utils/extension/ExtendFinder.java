package com.zoowii.jpa_utils.extension;

import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.Finder;
import com.zoowii.jpa_utils.query.JoinInfo;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * this is an extended Finder class, and user can define own Finder class extend from Finder to add features 
 * I suggest to define a seprate Finder sub class extend from Finder or ExtendFinder, then you can add kinds of query methods
 * Created by zoowii on 14-12-23.
 */
public class ExtendFinder<K, M> extends Finder<K, M> {
    public ExtendFinder(Class<?> kCls, Class<?> mCls) {
        super(kCls, mCls);
    }

    /**
     * simple method of find using simple Paginator class, if you need, you cant extend the Find class 
     *
     * @param paginator paginator instance to use
     * @return query result
     */
    public List<M> findByPaginator(Paginator paginator) {
        if (paginator == null) {
            return this.findAll();
        }
        Query<M> query = this.where();
        if(StringUtil.notEmpty(paginator.getTableAlias())) {
            query = query.alias(paginator.getTableAlias());
        }
        for (Expr expr : paginator.getExpressions()) {
            query = query.and(expr);
        }
        if(paginator.getJoinInfos()!=null) {
            for (JoinInfo joinInfo : paginator.getJoinInfos()) {
                query.addJoinInfo(joinInfo);
            }
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
