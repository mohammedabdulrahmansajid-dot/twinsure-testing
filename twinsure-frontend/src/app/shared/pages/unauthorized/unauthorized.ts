// Displays a clear access-denied page for incorrect role navigation.

import {
  ChangeDetectionStrategy,
  Component
} from '@angular/core';
import {
  RouterLink
} from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  imports: [
    RouterLink
  ],
  template: `
    <main class="flex min-h-screen items-center justify-center bg-slate-50 px-6">
      <section class="w-full max-w-lg rounded-3xl border border-slate-200 bg-white p-10 text-center shadow-sm">
        <p class="text-sm font-semibold uppercase tracking-widest text-red-700">
          Access denied
        </p>

        <h1 class="mt-3 text-3xl font-bold text-slate-900">
          This workspace is not assigned to your role
        </h1>

        <p class="mt-4 leading-7 text-slate-600">
          TwinSure has protected this route. Return to the home page or use
          the navigation provided for your authenticated role.
        </p>

        <a
          routerLink="/"
          class="mt-8 inline-flex rounded-xl bg-blue-700 px-5 py-3 font-semibold text-white hover:bg-blue-800"
        >
          Return home
        </a>
      </section>
    </main>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Unauthorized {
}