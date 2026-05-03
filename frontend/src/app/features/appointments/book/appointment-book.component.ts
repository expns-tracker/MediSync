import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { distinctUntilChanged, take } from 'rxjs';
import { AppointmentService } from '../../../core/services/appointment.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorDto } from '../../../core/models/doctor.models';
import { AppointmentBookDto } from '../../../core/models/appointment.models';

@Component({
  selector: 'app-appointment-book',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './appointment-book.component.html',
  styleUrls: ['./appointment-book.component.scss'],
})
export class AppointmentBookComponent implements OnInit {
  doctors = signal<DoctorDto[]>([]);
  availableSlots = signal<string[]>([]);
  isLoading = signal(false);
  isSlotLoading = signal(false);
  errorMessage = signal('');
  slotError = signal('');
  successMessage = signal('');
  patientId?: number;
  appointmentForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private appointmentService: AppointmentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.appointmentForm = this.fb.group({
      doctorId: [null as number | null, [Validators.required]],
      appointmentDate: ['', [Validators.required]],
      appointmentTimeSlot: ['', [Validators.required]],
      reason: ['', [Validators.required, Validators.minLength(10)]],
    });

    this.loadDoctors();
    this.authService.currentUser$.pipe(take(1)).subscribe((user) => {
      this.patientId = user?.patientId ?? undefined;
    });

    this.appointmentForm
      .get('doctorId')
      ?.valueChanges.pipe(distinctUntilChanged())
      .subscribe(() => this.loadAvailableSlots());

    this.appointmentForm
      .get('appointmentDate')
      ?.valueChanges.pipe(distinctUntilChanged())
      .subscribe(() => this.loadAvailableSlots());

    const doctorId = Number(this.route.snapshot.queryParamMap.get('doctorId'));
    if (doctorId) {
      this.appointmentForm.patchValue({ doctorId });
    }
  }

  submit(): void {
    if (this.appointmentForm.invalid) {
      this.appointmentForm.markAllAsTouched();
      return;
    }

    if (!this.patientId) {
      this.errorMessage.set('Could not load your patient profile. Please sign out and sign in again.');
      return;
    }

    const doctorId = Number(this.appointmentForm.get('doctorId')?.value);
    const appointmentDate = this.appointmentForm.get('appointmentDate')?.value ?? '';
    const appointmentTimeSlot = this.appointmentForm.get('appointmentTimeSlot')?.value ?? '';
    const reason = this.appointmentForm.get('reason')?.value ?? '';

    const appointment: AppointmentBookDto = {
      patientId: this.patientId,
      doctorId,
      appointmentTime: `${appointmentDate}T${appointmentTimeSlot}`,
      reason: reason.trim(),
    };

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.appointmentService.bookAppointment(appointment).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('Appointment booked successfully.');
        setTimeout(() => this.router.navigate(['/doctors']), 2000);
      },
      error: (error: Error) => {
        this.isLoading.set(false);
        this.errorMessage.set(error.message);
      },
    });
  }

  private loadDoctors(): void {
    this.doctorService.getDoctors().subscribe({
      next: (doctors) => this.doctors.set(doctors),
      error: (error: Error) => this.errorMessage.set(error.message),
    });
  }

  private loadAvailableSlots(): void {
    const doctorId = this.appointmentForm.get('doctorId')?.value;
    const appointmentDate = this.appointmentForm.get('appointmentDate')?.value;

    this.availableSlots.set([]);
    this.appointmentForm.get('appointmentTimeSlot')?.reset('');
    this.slotError.set('');

    if (doctorId == null || !appointmentDate) {
      return;
    }

    this.isSlotLoading.set(true);
    this.doctorService.getAvailableSlots(doctorId, appointmentDate).subscribe({
      next: (slots) => {
        this.isSlotLoading.set(false);
        this.availableSlots.set(slots);
      },
      error: (error: Error) => {
        this.isSlotLoading.set(false);
        this.slotError.set(error.message);
      },
    });
  }
}
