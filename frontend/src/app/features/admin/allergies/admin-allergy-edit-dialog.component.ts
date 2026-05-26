import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AllergyService } from '../../../core/services/allergy.service';
import { AllergyDto } from '../../../core/models/allergy.models';

@Component({
  selector: 'app-admin-allergy-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './admin-allergy-edit-dialog.component.html',
  styleUrls: ['./admin-allergy-edit-dialog.component.scss'],
})
export class AdminAllergyEditDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly allergyService = inject(AllergyService);
  private readonly dialogRef = inject(MatDialogRef<AdminAllergyEditDialogComponent>);
  private readonly data = inject<{ allergy?: AllergyDto }>(MAT_DIALOG_DATA);

  allergyForm: FormGroup;
  isEdit = false;
  isSaving = signal(false);

  categories = ['FOOD', 'MEDICATION', 'ENVIRONMENTAL', 'OTHER'];

  constructor() {
    this.isEdit = !!this.data.allergy;
    this.allergyForm = this.fb.group({
      name: [this.data.allergy?.name || '', [Validators.required, Validators.minLength(2)]],
      code: [this.data.allergy?.code || '', [Validators.required]],
      category: [this.data.allergy?.category || 'OTHER', [Validators.required]],
    });
  }

  onSubmit(): void {
    if (this.allergyForm.invalid) return;

    this.isSaving.set(true);
    const formValue = this.allergyForm.value;

    if (this.isEdit && this.data.allergy) {
      this.allergyService.updateAllergy(this.data.allergy.id, formValue).subscribe({
        next: () => {
          this.isSaving.set(false);
          this.dialogRef.close(true);
        },
        error: () => {
          this.isSaving.set(false);
        },
      });
    } else {
      this.allergyService.createAllergy(formValue).subscribe({
        next: () => {
          this.isSaving.set(false);
          this.dialogRef.close(true);
        },
        error: () => {
          this.isSaving.set(false);
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
