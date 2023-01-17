package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rules.RulesModel;

public interface RulesService {

    ContainerForList<RulesModel> getRules(String username, Long locationId) throws DescriptionException;
    void addRules(String username, Long locationId, String email) throws DescriptionException;
    void deleteRules(String username, Long locationId, String email) throws DescriptionException;
    public Boolean checkRulesInLocation(String username, Long locationId);
}
