import * as React from "react";

interface PageNumberProps {
  page: number;
  active: boolean;
  setPage: (pageIndex: number) => void;
}

export class PageNumber extends React.Component<PageNumberProps, {}> {

  private changePage = () => {
    this.props.setPage(this.props.page - 1);
  };

  public render() {
    const {page, active} = this.props;
    return (
      <li key={page} className={active ? "active" : "waves-effect"}>
        <a onClick={this.changePage}>{page}</a>
      </li>
    );
  };
}