import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TaskListComponent } from './components/task-list/task-list.component';
import { TaskDetailsComponent } from './components/task-details/task-details.component';
import { TaskFormComponent } from './components/task-form/task-form.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AdminComponent } from './components/admin/admin.component';
import { ModeratorComponent } from './components/moderator/moderator.component';
import { AuthGuard, AdminGuard, ModeratorGuard } from './helpers/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: 'tasks', pathMatch: 'full' },
  
  // Public routes
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  
  // Protected routes
  { path: 'tasks', component: TaskListComponent, canActivate: [AuthGuard] },
  { path: 'tasks/:id', component: TaskDetailsComponent, canActivate: [AuthGuard] },
  { path: 'add', component: TaskFormComponent, canActivate: [AuthGuard] },
  { path: 'edit/:id', component: TaskFormComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  
  // Role-based protected routes
  { path: 'admin', component: AdminComponent, canActivate: [AdminGuard] },
  { path: 'moderator', component: ModeratorComponent, canActivate: [ModeratorGuard] },
  
  { path: '**', redirectTo: 'tasks' } // Handle any invalid routes
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }