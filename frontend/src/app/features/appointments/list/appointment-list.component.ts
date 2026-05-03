import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AppointmentDto } from '../../../core/models/appointment.models';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './appointment-list.component.html',
  styleUrls: ['./appointment-list.component.scss'],
})
export class AppointmentListComponent {
  @Input() title = '';
  @Input() appointments: AppointmentDto[] = [];
}
