import BaseComponent from "../BaseComponent";
import React, {createRef} from "react";
import M, { ModalOptions } from "materialize-css";
import Form, {IFields, IValues} from "../form/Form";
import ScrollBar from "react-perfect-scrollbar";

interface InputDialogProps {
  id: string;
  title?: string;
  fields: IFields;
  values: IValues;
  position?: string;
  confirmCallback: (input: any) => void;
  fullscreen?: boolean;
  scrollbar?: React.RefObject<ScrollBar>;
}

type Props = InputDialogProps;

interface State {
  fullscreen: boolean;
  scrollMaxHeight: number;
}

export default class InputDialog extends BaseComponent<Props, State> {

  private modal = createRef<HTMLDivElement>();
  private scrollbar = this.props.scrollbar || createRef<ScrollBar>();

  constructor(props: Props) {
    super(props);
    this.state = {
      fullscreen: props.fullscreen || false,
      scrollMaxHeight: this.calcDialogScrollMaxHeight(props.fullscreen || false)
    }
  }

  private initModal = (position?: string, fullScreen?: boolean) => {
    let options: Partial<ModalOptions> = {
      onOpenEnd: this.onOpenModal,
      onCloseStart: this.onCloseModal,
    };
    if (position) {
      options.startingTop = position;
      options.endingTop = position;
    }
    if (fullScreen) {
      options.startingTop = '0%';
      options.endingTop = '0%';
    }
    M.Modal.init(this.modal.current as Element, options);
  }

  public componentDidMount(): void {
    this.initModal(this.props.position, this.state.fullscreen);
    window.addEventListener('resize', this.handleResize);
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    let modal = M.Modal.getInstance(this.modal.current as Element);
    if (prevState.fullscreen !== this.state.fullscreen
        || prevState.scrollMaxHeight !== this.state.scrollMaxHeight) {
      modal.options.inDuration = 0;
      modal.options.outDuration = 0;
      modal.options.onOpenEnd = this.onOpenModal;
      modal.options.onCloseStart =  this.onCloseModal;
    }
    this.scrollbar?.current?.updateScroll();
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.handleResize);
  }

  private calcDialogScrollMaxHeight = (fullscreen: boolean) =>
    fullscreen
      //116px because of 58px title and 58px footer
      ? document.body.clientHeight - 116
      : document.body.clientHeight * 0.7 - 116

  private handleResize = () => {
    this.setState({
      scrollMaxHeight: this.calcDialogScrollMaxHeight(this.state.fullscreen)
    })
    this.scrollbar?.current?.updateScroll();
  }

  private onOpenModal = (): void => {
    M.updateTextFields();
    this.scrollbar.current?.updateScroll();
  };

  private onCloseModal = (): void => {
    let modal = M.Modal.getInstance(this.modal.current as Element);
    modal.options.inDuration = 250;
    modal.options.outDuration = 250;
  }

  private confirmCallback = (values: IValues): void => {
    let modal = M.Modal.getInstance(this.modal.current as Element);
    modal.close();
    this.props.confirmCallback(values);
  };

  private toggleFullScreen = () => {
    this.setState({fullscreen: !this.state.fullscreen}, () => this.handleResize());
  }

  public render() {
    const {id, title, fields, values, children} = this.props;
    const {fullscreen} = this.state;
    return (
      <div id={id} className={`modal dialog ${fullscreen ? 'modal-fullscreen' : ''}`} ref={this.modal}>
        <div className="modal-content">
          {title && (
            <>
              <div className='modal-title'>
                {title}
                {!fullscreen && (
                  <button className='btn-floating btn-flat right'
                          onClick={this.toggleFullScreen}>
                    <i className="material-icons">fullscreen</i>
                  </button>
                )}
                {fullscreen && (
                  <>
                    <button className='modal-close btn-floating btn-flat right'>
                      <i className="material-icons">close</i>
                    </button>
                    <button className='btn-floating btn-flat right'
                            onClick={this.toggleFullScreen}>
                      <i className="material-icons">fullscreen_exit</i>
                    </button>
                  </>
                )}
              </div>
            </>
          )}
          <Form id='inputDialog'
                fields={fields}
                values={values}
                controlsMode={fullscreen ? 'modal-fullscreen' : 'modal'}
                modal={{
                  onConfirm: this.confirmCallback,
                  scrollbar: this.scrollbar,
                  scrollMaxHeight: this.state.scrollMaxHeight
                }}>
            {children}
          </Form>
        </div>
      </div>
    );
  }

}