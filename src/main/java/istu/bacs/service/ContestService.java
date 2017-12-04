package istu.bacs.service;

import istu.bacs.model.Contest;

import java.util.List;

public interface ContestService {
	
	Contest findById(Integer id);
	List<Contest> findAll();
	void save(Contest contest);
    void delete(Contest contest);

}