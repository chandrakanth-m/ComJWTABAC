import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-moderator',
  templateUrl: './moderator.component.html',
  styleUrls: ['./moderator.component.css']
})
export class ModeratorComponent implements OnInit {
  currentUser: User | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
  }

  isModerator(): boolean {
    return this.authService.isModerator();
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  hasModeratorAccess(): boolean {
    return this.isModerator() || this.isAdmin();
  }
}