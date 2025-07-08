import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Task } from '../../models/task.model';
import { TaskService } from '../../services/task.service';

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

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
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
          this.taskForm.patchValue({
            title: task.title,
            description: task.description,
            completed: task.completed
          });
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Error loading task data. Please try again later.';
          console.error('Error loading task data:', error);
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
    const taskData: Task = this.taskForm.value;

    if (this.isEditMode) {
      this.updateTask(taskData);
    } else {
      this.createTask(taskData);
    }
  }

  createTask(taskData: Task): void {
    this.taskService.createTask(taskData)
      .subscribe({
        next: () => {
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          this.errorMessage = 'Error creating task. Please try again.';
          console.error('Error creating task:', error);
          this.submitting = false;
        }
      });
  }

  updateTask(taskData: Task): void {
    this.taskService.updateTask(this.taskId!, taskData)
      .subscribe({
        next: () => {
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          this.errorMessage = 'Error updating task. Please try again.';
          console.error('Error updating task:', error);
          this.submitting = false;
        }
      });
  }

  get title() { return this.taskForm.get('title'); }
  get description() { return this.taskForm.get('description'); }
}