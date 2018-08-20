package com.sinosoftgz.demo.lucenedemo.vo;

import java.io.Serializable;

/**
 * 分页请求
 * @author akers
 *
 */
public class PageRequest implements Serializable {
	private static final long serialVersionUID = 8552474712437995564L;
	/**
	 * 每页数据量
	 */
	private final int pageSize;
	/**
	 * 当前页号
	 */
	private final int pageNum;
	
	public PageRequest(int pageNum, int pageSize) {

		if (pageNum < 0) {
			throw new IllegalArgumentException("Page index must not be less than zero!");
		}

		if (pageSize < 1) {
			throw new IllegalArgumentException("Page size must not be less than one!");
		}

		this.pageNum = pageNum;
		this.pageSize = pageSize;
	}
	
	public int getOffset() {
		return pageNum * pageSize;
	}

	public boolean hasPrevious() {
		return pageNum > 0;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}
	
	public PageRequest next() {
		return new PageRequest(getPageNum() + 1, getPageSize());
	}
}
