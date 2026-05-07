import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { AppointmentDto } from '../../../core/models/appointment.models';
import { AppointmentCancelDialogComponent } from '../cancel-dialog/cancel-dialog.component';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './appointment-list.component.html',
  styleUrls: ['./appointment-list.component.scss'],
})
export class AppointmentListComponent {
  private readonly dialog = inject(MatDialog);

  @Input() title = '';
  @Input() appointments: AppointmentDto[] = [];
  @Input() allowCancel = true;
  @Output() cancelRequested = new EventEmitter<number>();

  onCancelClick(appointmentId: number): void {
    const dialogRef = this.dialog.open(AppointmentCancelDialogComponent, {
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.cancelRequested.emit(appointmentId);
      }
    });
  }
}
