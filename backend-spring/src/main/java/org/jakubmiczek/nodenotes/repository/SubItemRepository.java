package org.jakubmiczek.nodenotes.repository;

import org.jakubmiczek.nodenotes.entity.SubItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SubItemRepository extends JpaRepository<SubItem, Long> {

}
