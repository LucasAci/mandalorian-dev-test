package br.com.lucas.controller;

import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import br.com.lucas.pojo.Task;
import br.com.lucas.service.TaskService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Path("/tasks")
public class TaskController {
	
	private final AtomicLong taskId = new AtomicLong(1);	

	  @Inject
	  TaskService client;

	  @GET
	  public Multi<Task> allCustomers() {
	    return client.allTasks();
	  }

	  @GET
	  @Path("/{id}")
	  public Uni<Task> getTask(@PathParam("id") Long id) {
	    return client.getTask(id).onItem().ifNull()
	        .failWith(new WebApplicationException("Failed to find customer", Response.Status.NOT_FOUND));
	  }

	  @POST
	  public Uni<Response> createCustomer(Task task) {		
	    if (task.id != null || task.name.length() < 10 || task.status.length() == 0) {
	      throw new WebApplicationException("Invalid task set on request", 422);
	    }

	    task.id = taskId.getAndIncrement();

	    return client.createTask(task)
	        .onItem().transform(tk -> Response.ok(tk).status(Response.Status.CREATED).build())
	        .onFailure().recoverWithItem(Response.serverError().build());
	  }

	  @PUT
	  @Path("/{id}")
	  public Uni<Response> updateTask(@PathParam("id") Long id, Task task) {
	    if (task.id == null || (task.name == null || task.name.length() == 0) || (task.status == null || task.status.length() == 0)) {
	      throw new WebApplicationException("Invalid task set on request", 422);
	    }

	    return client.updateTask(task)
	        .onItem().ifNotNull().transform(success -> Response.ok(task).build())
	        .onFailure().recoverWithItem(Response.ok().status(Response.Status.NOT_FOUND).build());
	  }

	  @DELETE
	  @Path("/{id}")
	  public Uni<Response> deleteTask(@PathParam("id") Long id) {
	    return client.deleteTask(id)
	        .onItem().transform(i -> Response.ok().status(Response.Status.NO_CONTENT).build())
	        .onFailure().recoverWithItem(Response.ok().status(Response.Status.NOT_FOUND).build());
	  }

}
