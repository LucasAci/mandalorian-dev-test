package br.com.lucas.service;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import br.com.lucas.exception.NotFoundException;
import br.com.lucas.pojo.Task;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Response;

@Singleton
public class TaskService {

	private static final String TASK_HASH_PREFIX = "task:";

	@Inject
	ReactiveRedisClient reactiveRedisClient;

	public Multi<Task> allTasks() {
		return reactiveRedisClient.keys("*").onItem()
				.transformToMulti(response -> Multi.createFrom().iterable(response).map(Response::toString)).onItem()
				.transformToUniAndMerge(key -> reactiveRedisClient.hgetall(key)
						.map(resp -> constructTask(Long.parseLong(key.substring(TASK_HASH_PREFIX.length())), resp)));
	}

	public Uni<Task> getTask(Long id) {
		return reactiveRedisClient.hgetall(TASK_HASH_PREFIX + id)
				.map(resp -> resp.size() > 0 ? constructTask(id, resp) : null);
	}

	public Uni<Task> createTask(Task task) {
		return storeTask(task);
	}

	public Uni<Task> updateTask(Task task) {
		return getTask(task.id).onItem().transformToUni((tk) -> {
			if (tk == null) {
				return Uni.createFrom().failure(new NotFoundException());
			}
			tk.name = task.name;
			tk.description = task.description;
			tk.status = task.status;
			return storeTask(tk);
		});
	}

	public Uni<Void> deleteTask(Long id) {
		return reactiveRedisClient.hdel(Arrays.asList(TASK_HASH_PREFIX + id, "name"))
				.map(resp -> resp.toInteger() == 1 ? true : null).onItem().ifNull().failWith(new NotFoundException())
				.onItem().ifNotNull().transformToUni(r -> Uni.createFrom().nullItem());
	}

	private Uni<Task> storeTask(Task task) {
		return reactiveRedisClient.hmset(Arrays.asList(TASK_HASH_PREFIX + task.id, "name", task.name, "description",
				task.description, "status", task.status)).onItem().transform(resp -> {
					if (resp.toString().equals("OK")) {
						return task;
					} else {
						throw new NoSuchElementException();
					}
				});
	}

	Task constructTask(long id, Response response) {
		Task task = new Task();
		task.id = id;
		task.name = response.get("name").toString();
		task.description = response.get("description").toString();
		task.status = response.get("status").toString();
		return task;
	}

}
