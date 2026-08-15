package com.casacrew.repository;

import com.casacrew.model.CleaningTask;
import com.casacrew.model.TaskPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskPhotoRepository extends JpaRepository<TaskPhoto, Long> {
    List<TaskPhoto> findByTaskOrderByUploadedAtDesc(CleaningTask task);
}
