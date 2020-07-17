import React, {createRef} from "react";
import M from "materialize-css";
import ResizeObserver from 'resize-observer-polyfill';

interface Props {
  id: string;
  title: string;
  active?: boolean;
  headerClassname?: string;
  bodyClassname?: string;
  onChange?: () => void;
}

interface State {
  isOpen: boolean;
}

export default class extends React.Component<Props, State> {

  state: State = {
    isOpen: this.props.active || false
  };

  private collapsible = createRef<HTMLUListElement>();
  private resizeObserver: (ResizeObserver | null) = null;

  public componentDidMount(): void {
    this.initCollapsible();
    if (this.props.onChange) {
      this.resizeObserver = new ResizeObserver(() => {
        this.props.onChange?.();
      });
      this.resizeObserver.observe(this.collapsible.current as Element);
    }
  }

  private initCollapsible = () => {
    M.Collapsible.init(this.collapsible.current as Element);
  };

  componentWillUnmount() {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
    }
  }

  private onChange = () => {
    this.setState(state => ({isOpen: !state.isOpen}));
    this.props.onChange?.();
  }


  public render() {
    const {id, title, active, headerClassname, bodyClassname, children} = this.props;
    const {isOpen} = this.state;
    return (
      <ul id={id} className="collapsible" ref={this.collapsible} onClick={this.onChange}>
        <li className={active ? "active" : undefined}>
          <ul className={`collapsible-header no-select ${headerClassname}`}>
            <div className={`subtitle`}>{title}</div>
            <i className="material-icons">{isOpen ? 'arrow_drop_down' : 'arrow_drop_up'}</i>
          </ul>
          <div className={`collapsible-body ${bodyClassname}`}>
            {children}
          </div>
        </li>
      </ul>
    );
  }

}