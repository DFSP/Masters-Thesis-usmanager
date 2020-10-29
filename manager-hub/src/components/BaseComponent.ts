/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React from "react";
import M from "materialize-css";

export default class BaseComponent<P, S> extends React.Component<P, S> {

    private toasts: { id: number, toast: M.Toast }[] = [];

    public componentWillUnmount(): void {
        this.toasts.forEach(toast => toast.toast.timeRemaining = 1000);
    }

    private removeToast = (id: number) => () =>
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

    public toast(message: string, displayLength: number = 6000, error?: string,
          instance?: boolean, unique?: boolean): void {
        const id = this.getToastId();
        const html = `<div>${message}${error ? `: <b class="red-text">${error}</b>` : ''}</div>`;
        if (!unique || !this.toasts.some(toast => toast.toast.options.html === html)) {
            const toast = M.toast({html, displayLength, completeCallback: this.removeToast(id), classes: 'test'});
            if (instance || unique) {
                this.toasts.push({id: id, toast: toast});
            }
        }
    };

}
