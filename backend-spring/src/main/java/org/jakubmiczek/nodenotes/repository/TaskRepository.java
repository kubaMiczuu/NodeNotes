package org.jakubmiczek.nodenotes.repository;

import org.jakubmiczek.nodenotes.entity.Task;
import org.jakubmiczek.nodenotes.entity.TaskStatus;
import org.jakubmiczek.nodenotes.entity.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT task FROM Task task WHERE task.user.username = :username " +
    "AND (:status IS NULL OR task.status = :status) " +
    "AND (:type IS NULL OR task.type = :type) " +
    "AND (:title IS NULL OR  :title = '' OR LOWER(task.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Task> findTaskWithFilters(
            @Param("username") String username,
            @Param("status") TaskStatus status,
            @Param("type") TaskType taskType,
            @Param("title") String title,
            Pageable pageable
    );

}
