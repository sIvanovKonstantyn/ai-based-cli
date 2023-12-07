
package com.aigenerated.modelname.repositories;

import com.aigenerated.modelname.model.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TaskRepository extends CrudRepository<Task, Long> {
    Task save(Task task);
}
