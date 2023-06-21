import { Card } from "react-bootstrap";

const Index = () => {
  return (
    <>
    <div
        className="d-flex justify-content-center align-items-center"
        style={{ minHeight: "500px", minWidth: "600px" }}
      >
        <Card>
          <Card.Body>
            <Card.Text>
              Welcome to Knowldge Islands. This is a open-source software that analyze developer's knowledge distribution on GitHub repositories.  
            </Card.Text>
          </Card.Body>
        </Card>
        </div>
    </>
  )
};

export default Index;