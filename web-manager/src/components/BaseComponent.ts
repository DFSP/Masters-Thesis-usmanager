import React from "react";
import M from "materialize-css";

export default class BaseComponent<P, S> extends React.Component<P, S> {

  private toasts: {id: number, toast: M.Toast}[] = [];

  public componentWillUnmount(): void {
    this.toasts.forEach(toast => toast.toast.timeRemaining = 1000);
  }

  removeToast = (id: number) => () =>
    this.toasts.forEach((toast, index) => {
        if (toast.id === id) {
          return delete this.toasts[index];
        }
      }
    );

  private getToastId = (): number => {
    for (let i = 0; ; i++) {
      if (!this.toasts[i]) {
        return i;
      }
    }
  }

  toast(message: string, displayLength: number = 6000, error?: string,
        instance?: boolean, unique?: boolean): void {
    const id = this.getToastId();
    const html = `<div>${message}${error ? `: <b class="red-text">${error}</b>` : ''}</div>`;
    if (!unique || !this.toasts.some(toast => toast.toast.options.html === html)) {
      const toast = M.toast({ html , displayLength,  completeCallback: this.removeToast(id), classes:'test'});
      if (instance || unique) {
        this.toasts.push({id: id, toast: toast});
      }
    }
  };

}
