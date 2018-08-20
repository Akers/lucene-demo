package com.sinosoftgz.demo.lucenedemo.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 分页结果集
 * @author akers
 *
 * @param <T>
 */
public class PageResult<T> implements Iterable<T>, Serializable {
	private final long total;
	private final List<T> content;
	private final PageRequest pageable;
	
	/**
	 * 分页结果集初始化
	 * @param content
	 * @param pageable
	 * @param total
	 */
	public PageResult(List<T> content, PageRequest pageable, long total) {
		this.pageable = pageable;
		this.content = content;
		
		this.total = !content.isEmpty() && pageable != null && pageable.getOffset() + pageable.getPageSize() > total
				? pageable.getOffset() + content.size() : total;
	}
	
	public PageResult(List<T> content) {
		this(content, null, null == content ? 0 : content.size());
	}
	
	/**
	 * 每页数据量
	 * @return
	 */
	public int getSize() {
		return pageable == null ? 0 : pageable.getPageSize();
	}
	
	/**
	 * 当前页数
	 * @return
	 */
	public int getNumber() {
		return pageable == null ? 0 : pageable.getPageNum();
	}
	
	/**
	 * 总数据页数
	 * @return
	 */
	public int getTotalPages() {
		return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
	}

	/**
	 * 总数据量
	 * @return
	 */
	public long getTotalElements() {
		return total;
	}

	/**
	 * 是否存在下一页
	 * @return
	 */
	public boolean hasNext() {
		return getNumber() + 1 < getTotalPages();
	}

	/**
	 * 是否最后一页
	 * @return
	 */
	public boolean isLast() {
		return !hasNext();
	}
	
	/**
	 * 获取下一页的分页请求
	 * @return
	 */
	public PageRequest nextPageRequest() {
		return hasNext() ? pageable.next() : null;
	}

	@Override
	public Iterator<T> iterator() {
		return content == null ? null : content.iterator();
	}
}
