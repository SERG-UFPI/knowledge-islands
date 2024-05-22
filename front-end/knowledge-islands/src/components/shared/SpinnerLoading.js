import Spinner from 'react-bootstrap/Spinner';

const SpinnerLoading = () => {
  return (
    <Spinner animation="border" role="status">
      <span className="visually-hidden">Loading...</span>
    </Spinner>
  );
}

export default SpinnerLoading;