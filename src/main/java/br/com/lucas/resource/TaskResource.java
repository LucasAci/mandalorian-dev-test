package br.com.lucas.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;

import br.com.lucas.pojo.Task;
import br.com.lucas.service.TaskService;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import java.util.List;

import io.smallrye.mutiny.Uni;

@Path("/task")
public class TaskResource {
	
	@Inject
    TaskService service;

    @GET
    public Uni<List<String>> keys() {
        return service.keys();
    }

    @POST
    public Task create(Task increment) {
        service.set(increment.key, increment.value);
        return increment;
    }

    @GET
    @Path("/{key}")
    public Task get(@PathParam("key") String key) {
        return new Task(key,service.get(key));
    }

    @PUT
    @Path("/{key}")
    public void increment(@PathParam("key") String key, Integer value) {
        service.task(key, value);
    }

    @DELETE
    @Path("/{key}")
    public Uni<Void> delete(@PathParam("key") String key) {
        return service.del(key);
    }
	
	

}
