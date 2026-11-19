import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import {PRIMEUI_LICENSE} from './primeui-license';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(),
    providePrimeNG({
      license: PRIMEUI_LICENSE,
      theme: {
        preset: Aura,
        options: { darkModeSelector: '.app-dark', cssLayer: { name: 'primeng', order: 'theme, base, primeng' } },
      },
    }),
  ]
};
