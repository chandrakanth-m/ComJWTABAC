import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task.service';

@Component({
  selector: 'app-task-details',
  templateUrl: './task-details.component.html',
  styleUrls: ['./task-details.component.css']
})
export class TaskDetailsComponent implements OnInit {
  task: Task | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private taskService: TaskService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.getTask();
  }

  getTask(): void {
    this.loading = true;
    const id = Number(this.route.snapshot.paramMap.get('id'));
    
    this.taskService.getTaskById(id)
      .subscribe({
        next: (data) => {
          this.task = data;
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Error loading task details. Please try again later.';
          console.error('Error loading task details:', error);
          this.loading = false;
        }
      });
  }

  toggleTaskStatus(): void {
    if (this.task) {
      this.taskService.toggleTaskStatus(this.task)
        .subscribe({
          next: (updatedTask) => {
            this.task = updatedTask;
          },
          error: (error) => {
            console.error('Error updating task status:', error);
            this.errorMessage = 'Error updating task status. Please try again.';
          }
        });
    }
  }

  deleteTask(): void {
    if (this.task && confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(this.task.id!)
        .subscribe({
          next: () => {
            this.router.navigate(['/tasks']);
          },
          error: (error) => {
            console.error('Error deleting task:', error);
            this.errorMessage = 'Error deleting task. Please try again.';
          }
        });
    }
  }
}