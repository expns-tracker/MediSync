import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  template: `
    <app-navbar></app-navbar>

    <div class="landing-page">
      <!-- Hero Section -->
      <section class="hero">
        <div class="hero-content">
          <h1>Your Healthcare, Streamlined</h1>
          <p class="hero-subtitle">
            Book appointments, manage medical records, and connect with doctors
            all in one place
          </p>
          <div class="hero-buttons">
            <a routerLink="/register" class="btn-primary">Get Started</a>
            <a href="#features" class="btn-secondary">Learn More</a>
          </div>
        </div>
        <div class="hero-image">
          <svg viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="medicalGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
              </linearGradient>
            </defs>
            <circle cx="200" cy="200" r="150" fill="url(#medicalGradient)" opacity="0.1"/>
            <circle cx="200" cy="160" r="25" fill="#667eea"/>
            <rect x="175" y="190" width="50" height="80" rx="25" fill="#764ba2"/>
            <circle cx="200" cy="240" r="15" fill="#667eea"/>
            <circle cx="200" cy="260" r="15" fill="#667eea"/>
            <circle cx="200" cy="280" r="15" fill="#667eea"/>
            <circle cx="200" cy="300" r="15" fill="#667eea"/>
          </svg>
        </div>
      </section>

      <!-- Features Section -->
      <section class="features" id="features">
        <div class="features-container">
          <h2>Why Choose MediSync?</h2>
          <div class="features-grid">
            <div class="feature-card">
              <div class="feature-icon">📅</div>
              <h3>Easy Appointment Booking</h3>
              <p>
                Schedule appointments with your preferred doctors instantly.
                Get reminders and manage your healthcare calendar effortlessly.
              </p>
            </div>

            <div class="feature-card">
              <div class="feature-icon">📋</div>
              <h3>Medical Records</h3>
              <p>
                Keep all your medical history and prescriptions in one secure
                place. Access your health information anytime, anywhere.
              </p>
            </div>

            <div class="feature-card">
              <div class="feature-icon">🏥</div>
              <h3>Find Doctors</h3>
              <p>
                Search doctors by specialty and department. Check their
                availability instantly.
              </p>
            </div>

            <div class="feature-card">
              <div class="feature-icon">🔐</div>
              <h3>Secure & Private</h3>
              <p>
                Your health information is encrypted and protected with
                enterprise-grade security.
              </p>
            </div>

            <div class="feature-card">
              <div class="feature-icon">⏰</div>
              <h3>24/7 Access</h3>
              <p>
                Access your appointments and medical information anytime,
                anywhere.
              </p>
            </div>

            <div class="feature-card">
              <div class="feature-icon">💬</div>
              <h3>Communication</h3>
              <p>
                Stay informed with appointment reminders and doctor
                communications.
              </p>
            </div>
          </div>
        </div>
      </section>

      <!-- About Section -->
      <section class="about" id="about">
        <div class="about-container">
          <div class="about-content">
            <h2>About MediSync</h2>
            <p>
              MediSync is a comprehensive hospital management system designed to
              streamline healthcare delivery and improve patient experience. Our
              platform connects patients, doctors, and healthcare providers in a
              secure, efficient, and user-friendly environment.
            </p>
            <p>
              With MediSync, you can easily book appointments, access your medical
              records, communicate with healthcare professionals, and manage your
              healthcare journey from the comfort of your home.
            </p>
          </div>
        </div>
      </section>

      <!-- Footer -->
      <footer class="footer">
        <div class="footer-container">
          <div class="footer-content">
            <h3>MediSync</h3>
            <p>Your trusted healthcare companion</p>
          </div>
          <div class="footer-links">
            <a href="#features">Features</a>
            <a href="#about">About</a>
            <a routerLink="/login">Sign In</a>
            <a routerLink="/register">Register</a>
          </div>
          <div class="footer-bottom">
            <p>&copy; 2026 MediSync. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  `,
  styles: `
    .landing-page {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
        Ubuntu, Cantarell, sans-serif;
      color: #333;
      line-height: 1.6;
    }

    /* ============ Hero Section ============ */
    .hero {
      max-width: 1200px;
      margin: 0 auto;
      padding: 80px 20px;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 40px;
      align-items: center;
    }

    .hero-content h1 {
      font-size: 48px;
      font-weight: 700;
      margin: 0 0 20px;
      line-height: 1.2;
    }

    .hero-subtitle {
      font-size: 18px;
      color: #666;
      margin: 0 0 30px;
    }

    .hero-buttons {
      display: flex;
      gap: 15px;
    }

    .btn-primary {
      padding: 14px 32px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 4px;
      text-decoration: none;
      font-weight: 600;
      font-size: 15px;
      cursor: pointer;
      transition: transform 0.3s, box-shadow 0.3s;
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
    }

    .btn-secondary {
      padding: 14px 32px;
      background: transparent;
      color: #667eea;
      border: 2px solid #667eea;
      border-radius: 4px;
      text-decoration: none;
      font-weight: 600;
      font-size: 15px;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-secondary:hover {
      background: #667eea;
      color: white;
    }

    .hero-image svg {
      width: 100%;
      max-width: 400px;
      height: auto;
    }

    /* ============ Features Section ============ */
    .features {
      background: #f8f9fa;
      padding: 80px 0;
    }

    .features-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }

    .features h2 {
      font-size: 36px;
      font-weight: 700;
      text-align: center;
      margin: 0 0 60px;
      color: #333;
    }

    .features-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 40px;
    }

    .feature-card {
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
      text-align: center;
      transition: transform 0.3s;
    }

    .feature-card:hover {
      transform: translateY(-5px);
    }

    .feature-icon {
      font-size: 48px;
      margin-bottom: 20px;
    }

    .feature-card h3 {
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 15px;
      color: #333;
    }

    .feature-card p {
      color: #666;
      margin: 0;
      line-height: 1.6;
    }

    /* ============ About Section ============ */
    .about {
      padding: 80px 0;
    }

    .about-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }

    .about-content h2 {
      font-size: 36px;
      font-weight: 700;
      text-align: center;
      margin: 0 0 40px;
      color: #333;
    }

    .about-content p {
      font-size: 18px;
      line-height: 1.8;
      color: #666;
      max-width: 800px;
      margin: 0 auto 20px;
      text-align: center;
    }

    /* ============ Footer ============ */
    .footer {
      background: #2d3748;
      color: white;
      padding: 40px 0 20px;
    }

    .footer-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }

    .footer-content h3 {
      font-size: 24px;
      font-weight: 600;
      margin: 0 0 10px;
    }

    .footer-content p {
      color: #a0aec0;
      margin: 0 0 30px;
    }

    .footer-links {
      display: flex;
      gap: 30px;
      margin-bottom: 30px;
      flex-wrap: wrap;
    }

    .footer-links a {
      color: #a0aec0;
      text-decoration: none;
      transition: color 0.3s;
    }

    .footer-links a:hover {
      color: white;
    }

    .footer-bottom {
      border-top: 1px solid #4a5568;
      padding-top: 20px;
      text-align: center;
      color: #a0aec0;
    }

    /* ============ Responsive Design ============ */
    @media (max-width: 768px) {
      .hero {
        grid-template-columns: 1fr;
        text-align: center;
        gap: 60px;
      }

      .hero-content h1 {
        font-size: 36px;
      }

      .hero-buttons {
        justify-content: center;
      }

      .features-grid {
        grid-template-columns: 1fr;
      }

      .footer-links {
        flex-direction: column;
        gap: 15px;
      }
    }

    @media (max-width: 480px) {
      .hero {
        padding: 60px 20px;
      }

      .hero-content h1 {
        font-size: 28px;
      }

      .hero-subtitle {
        font-size: 16px;
      }

      .hero-buttons {
        flex-direction: column;
        align-items: center;
      }

      .features {
        padding: 60px 0;
      }

      .features h2 {
        font-size: 28px;
      }

      .feature-card {
        padding: 20px;
      }

      .about {
        padding: 60px 0;
      }

      .about-content h2 {
        font-size: 28px;
      }
    }
  `,
})
export class LandingComponent implements OnInit, OnDestroy {
  private authSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Subscribe to auth changes for dynamic updates (in case user logs out while on page)
    this.authSubscription = this.authService.isAuthenticated$.subscribe(isAuth => {
      if (isAuth) {
        this.router.navigate(['/home']);
      }
    });
  }

  ngOnDestroy() {
    this.authSubscription?.unsubscribe();
  }
}