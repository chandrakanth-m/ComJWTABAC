import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-task-details',
  templateUrl: './task-details.component.html',
  styleUrls: ['./task-details.component.css']
})
export class TaskDetailsComponent implements OnInit {
  task: Task | null = null;
  loading = false;
  errorMessage = '';
  currentUser: User | null = null;

  constructor(
    private taskService: TaskService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }
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
    if (this.task && this.canEditTask()) {
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
    } else if (this.task && !this.canEditTask()) {
      this.errorMessage = 'You do not have permission to edit this task.';
    }
  }

  deleteTask(): void {
    if (this.task && this.canDeleteTask() && confirm('Are you sure you want to delete this task?')) {
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
    } else if (this.task && !this.canDeleteTask()) {
      this.errorMessage = 'You do not have permission to delete this task.';
    }
  }

  // ABAC Helper Methods
  canEditTask(): boolean {
    return this.task ? this.taskService.canEditTask(this.task) : false;
  }

  canDeleteTask(): boolean {
    return this.task ? this.taskService.canDeleteTask(this.task) : false;
  }

  isOwnTask(): boolean {
    return this.task && this.currentUser && this.task.owner ? 
      this.task.owner.username === this.currentUser.username : false;
  }

  isAdmin(): boolean {
    return this.currentUser ? this.currentUser.roles.includes('ADMIN') : false;
  }

  isModerator(): boolean {
    return this.currentUser ? this.currentUser.roles.includes('MODERATOR') : false;
  }

  getTaskOwnerDisplay(): string {
    if (!this.task) return 'Unknown';
    return this.isOwnTask() ? 'You' : (this.task.owner?.username || 'Unknown');
  }

  getPermissionLevel(): string {
    if (!this.task) return 'No Access';
    if (this.canDeleteTask()) return 'Full Access';
    if (this.canEditTask()) return 'Edit Access';
    return 'Read Only';
  }
}