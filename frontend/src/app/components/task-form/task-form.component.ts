import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-task-form',
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  taskForm!: FormGroup;
  isEditMode = false;
  taskId?: number;
  loading = false;
  submitting = false;
  errorMessage = '';
  formSubmitted = false;
  currentUser: User | null = null;
  currentTask: Task | null = null;

  constructor(
    private fb: FormBuilder,
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
    this.initForm();
    this.checkEditMode();
  }

  initForm(): void {
    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
      completed: [false]
    });
  }

  checkEditMode(): void {
    if (this.route.snapshot.paramMap.has('id')) {
      this.isEditMode = true;
      this.taskId = Number(this.route.snapshot.paramMap.get('id'));
      this.loadTaskData();
    }
  }

  loadTaskData(): void {
    this.loading = true;
    this.taskService.getTaskById(this.taskId!)
      .subscribe({
        next: (task) => {
          this.currentTask = task;
          
          // Check if user can edit this task
          if (!this.taskService.canEditTask(task)) {
            this.errorMessage = 'You do not have permission to edit this task.';
            this.loading = false;
            setTimeout(() => {
              this.router.navigate(['/tasks']);
            }, 2000);
            return;
          }
          
          this.taskForm.patchValue({
            title: task.title,
            description: task.description,
            completed: task.completed
          });
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading task:', error);
          this.errorMessage = 'Error loading task data. Please try again.';
          this.loading = false;
        }
      });
  }

  onSubmit(): void {
    this.formSubmitted = true;
    
    if (this.taskForm.invalid) {
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    const taskData = this.taskForm.value;

    if (this.isEditMode) {
      this.updateTask(taskData);
    } else {
      this.createTask(taskData);
    }
  }

  createTask(taskData: Omit<Task, 'id' | 'owner'>): void {
    this.taskService.createTask(taskData)
      .subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Error creating task. Please try again.';
          console.error('Error creating task:', error);
          this.submitting = false;
        }
      });
  }

  updateTask(taskData: Partial<Task>): void {
    this.taskService.updateTask(this.taskId!, taskData)
      .subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Error updating task. Please try again.';
          console.error('Error updating task:', error);
          this.submitting = false;
        }
      });
  }

  // Helper methods
  get title() { return this.taskForm.get('title'); }
  get description() { return this.taskForm.get('description'); }
  get completed() { return this.taskForm.get('completed'); }

  canEditCurrentTask(): boolean {
    return this.currentTask ? this.taskService.canEditTask(this.currentTask) : true;
  }

  getPageTitle(): string {
    return this.isEditMode ? 'Edit Task' : 'Create New Task';
  }

  getSubmitButtonText(): string {
    if (this.submitting) {
      return this.isEditMode ? 'Updating...' : 'Creating...';
    }
    return this.isEditMode ? 'Update Task' : 'Create Task';
  }
}