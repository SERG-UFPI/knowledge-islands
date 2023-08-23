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
              Welcome to Knowldge Islands. This is a open-source software that analyze developer's knowledge distribution on GitHub repositories. This tool implements source code knowledge models published in the following studies.
              <br/><br/>
              <i>Cury, Otávio, et al. "Identifying Source Code File Experts." Proceedings of the 16th ACM/IEEE International Symposium on Empirical Software Engineering and Measurement. 2022.</i>
              <br/><br/>
              This tool is being developed in <a href="https://github.com/OtavioCury/git-analyzer">github.com/OtavioCury/git-analyzer</a>
            </Card.Text>
          </Card.Body>
        </Card>
        </div>
    </>
  )
};

export default Index;