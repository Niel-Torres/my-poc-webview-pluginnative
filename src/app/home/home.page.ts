import { AfterViewInit, Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonFooter, IonButton, IonMenu, IonButtons, IonMenuButton } from '@ionic/angular/standalone';
import { registerPlugin } from '@capacitor/core';

type Rect = { x: number; y: number; width: number; height: number; };
type CreateOpts = Rect & { url: string };

const NativeWebView = registerPlugin<{
  create(opts: CreateOpts): Promise<void>;
  setRect(opts: Rect): Promise<void>;
  show(): Promise<void>;
  hide(): Promise<void>;
  destroy(): Promise<void>;
}>('NativeWebView'); // 👈 nombre debe coincidir con el nativo

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [IonButtons, IonHeader, IonToolbar, IonTitle, IonContent, IonFooter, IonButton, IonMenu, IonMenuButton],
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.scss'],
})
export class HomePage implements AfterViewInit {
  @ViewChild('nativeHost', { read: ElementRef }) nativeHost!: ElementRef<HTMLDivElement>;
  @ViewChild(IonMenu, { static: true }) menu!: IonMenu;

  ngAfterViewInit() {
    requestAnimationFrame(() => {
      this.positionNative();
    });
    // Detectar apertura/cierre del menú
    if (this.menu) {
      this.menu.ionDidOpen.subscribe(() => this.onMenuOpen());
      this.menu.ionDidClose.subscribe(() => this.onMenuClose());
    }
  }

  onMenuOpen() {
    console.log('Menú lateral ABIERTO');
    NativeWebView.hide();
  }

  onMenuClose() {
    console.log('Menú lateral CERRADO');
    NativeWebView.show();
  }

  @HostListener('window:resize') onResize() { NativeWebView.setRect(this.getRect()); }

  getRect(): Rect {
    const el = this.nativeHost.nativeElement;
    const rect = el.getBoundingClientRect();
    const scale = window.devicePixelRatio || 1;  // 👈 clave
    return {
      x: rect.left * scale,
      y: rect.top * scale,
      width: rect.width * scale,
      height: rect.height * scale,
    };
  }

  private positionNative() {
    const r = this.getRect();
    NativeWebView.create({ url: 'https://ws111.juntadeandalucia.es/catalogoplayas/visorplayas/', ...r });
  }

  // (Opcional) si usas Ionic Router, este hook también va muy bien
  ionViewDidEnter() {
    const r = this.getRect();
    NativeWebView.setRect(r); // por si el tamaño cambió tras transiciones
  }

  loadUrl() { NativeWebView.create({ url: 'https://ws111.juntadeandalucia.es/catalogoplayas/visorplayas/', ...this.getRect() }); }
  hide() { return NativeWebView.hide(); }
  show() { return NativeWebView.show(); }
  ionViewDidLeave() { NativeWebView.destroy(); }
}
