import { Component, OnInit } from '@angular/core';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task.service';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  loading = false;
  errorMessage = '';

  constructor(private taskService: TaskService) { }

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading = true;
    this.taskService.getAllTasks()
      .subscribe({
        next: (data) => {
          this.tasks = data;
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Error loading tasks. Please try again later.';
          console.error('Error loading tasks:', error);
          this.loading = false;
        }
      });
  }

  toggleTaskStatus(task: Task): void {
    this.taskService.toggleTaskStatus(task)
      .subscribe({
        next: (updatedTask) => {
          // Update the task in the list
          const index = this.tasks.findIndex(t => t.id === updatedTask.id);
          if (index !== -1) {
            this.tasks[index] = updatedTask;
          }
        },
        error: (error) => {
          console.error('Error updating task status:', error);
          this.errorMessage = 'Error updating task status. Please try again.';
        }
      });
  }

  deleteTask(id: number): void {
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(id)
        .subscribe({
          next: () => {
            this.tasks = this.tasks.filter(task => task.id !== id);
          },
          error: (error) => {
            console.error('Error deleting task:', error);
            this.errorMessage = 'Error deleting task. Please try again.';
          }
        });
    }
  }
}