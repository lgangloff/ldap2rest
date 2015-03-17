package org.ldap.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 
 * Liste paginée
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 */
public class PaginateList<E> implements Iterable<E>{

	private List<E> paginateResults;

	private List<E> allResults;


	private int startPage;
	private int pageSize;

	public PaginateList() {
		this(-1,-1);
	}

	
	public PaginateList(int startPage, int pageSize) {
		this.paginateResults = new ArrayList<E>();
		this.startPage = startPage;
		this.pageSize = pageSize;
	}


	private List<E> getResults() {
		return paginateResults.size() == 0 ? allResults : paginateResults;
	}

	public int getPageSize() {
		return (0 < pageSize && pageSize < getTotal()) ? pageSize : getTotal();
	}

	public int getTotal() {
		return allResults.size();
	}

	public int getStartPage() {
		return startPage <= -1 || startPage > getLastPage() ? 1 : startPage;
	}

	public int getLastPage() {
		if (getTotal() == 0) return 1;
		
		return getTotal() / getPageSize()
				+ (getTotal() % getPageSize() > 0 ? 1 : 0);
	}

	public void setResults(List<E> list) {
		this.allResults = list;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	private int getStartIndex() {
		int startIndex = (getPageSize() * (getStartPage() - 1));
		return (startIndex < 0 || startIndex > getTotal()) ? 0 : startIndex;
	}

	private int getEndIndex() {
		int endIndex = getPageSize() * getStartPage();
		return (endIndex < 0 || endIndex > getTotal() || endIndex < getStartIndex()) ? getTotal()
				: endIndex;
	}

	public void paginate() {
		this.paginateResults = new ArrayList<E>();
		paginateResults.addAll(allResults.subList(this.getStartIndex(),	this.getEndIndex()));
	}

	@Override
	public Iterator<E> iterator() {
		return getResults().iterator();
	}


	@Override
	public void forEach(Consumer<? super E> c) {
		getResults().forEach(c);
	}


	@Override
	public Spliterator<E> spliterator() {
		return getResults().spliterator();
	}	
}
