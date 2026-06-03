import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { AllergyService } from '../../../core/services/allergy.service';
import { AllergyDto } from '../../../core/models/allergy.models';
import { AdminAllergyEditDialogComponent } from './admin-allergy-edit-dialog.component';

@Component({
  selector: 'app-admin-allergy-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatChipsModule,
  ],
  templateUrl: './admin-allergy-list.component.html',
  styleUrls: ['./admin-allergy-list.component.scss'],
})
export class AdminAllergyListComponent implements OnInit {
  private readonly allergyService = inject(AllergyService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  allergies = signal<AllergyDto[]>([]);
  isLoading = signal(false);

  displayedColumns: string[] = ['code', 'name', 'category', 'actions'];

  ngOnInit(): void {
    this.loadAllergies();
  }

  loadAllergies(): void {
    this.isLoading.set(true);
    this.allergyService.getAllergies().subscribe({
      next: (data) => {
        this.allergies.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.snackBar.open(err.message || 'Failed to load allergies', 'Close');
        this.isLoading.set(false);
      },
    });
  }

  openEditDialog(allergy?: AllergyDto): void {
    const dialogRef = this.dialog.open(AdminAllergyEditDialogComponent, {
      width: '500px',
      data: { allergy },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadAllergies();
      }
    });
  }

  deleteAllergy(allergy: AllergyDto): void {
    if (confirm(`Are you sure you want to delete the allergy: ${allergy.name}?`)) {
      this.allergyService.deleteAllergy(allergy.id).subscribe({
        next: () => {
          this.snackBar.open('Allergy deleted successfully', 'Close', { duration: 3000 });
          this.loadAllergies();
        },
        error: (err) => {
          this.snackBar.open(err.message || 'Failed to delete allergy', 'Close');
        },
      });
    }
  }

  getCategoryColor(category: string): string {
    switch (category.toUpperCase()) {
      case 'FOOD': return 'orange';
      case 'MEDICATION': return 'red';
      case 'ENVIRONMENTAL': return 'green';
      default: return 'gray';
    }
  }
}
