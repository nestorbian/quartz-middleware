package com.unionpay.quartz.task.entity;

import lombok.Getter;
import lombok.Setter;

public class ResultWithPagination {
    
    // 当前页
    private int pageNumber = 1;
    
    // 当前页面条数
    @Getter
    private int pageSize = 10;
    
    // 结果集
    @Getter@Setter
    private Object results;
    
    // 总记录数
    @Getter@Setter
    private long totalCounts;
    
    @Setter
    private int totalPages;
    
	public ResultWithPagination(Integer pageNumber, Integer pageSize, long totalCounts) {
		super();
		setPageSize(pageSize);
		this.totalCounts = totalCounts;
		setPageNumber(pageNumber);
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public long getOffset() {
		return (getPageNumber() - 1) * getPageSize();
	}
	
    public void setPageNumber(Integer pageNumber) {
		if (pageNumber < 1) {
			this.pageNumber = 1;
		} else if (pageNumber > getTotalPages()) {
	        this.pageNumber = getTotalPages();
		} else {
			this.pageNumber = pageNumber;
		}
    }
	
    public void setPageSize(Integer pageSize) {
		if (pageSize < 1) {
			this.pageSize = 10;
		} else {
	        this.pageSize = pageSize;
		}
    }

	public int getTotalPages() {
		return (int) ((totalCounts - 1)/getPageSize() + 1);
	}

}
