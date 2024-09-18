import { Card } from "react-bootstrap";
import Image from 'react-bootstrap/Image';
import logo from "../assets/images/logo.png"

const Index = () => {
  return (
    <>
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ minHeight: "500px", minWidth: "600px" }}
      >
        <Image src={logo} roundedCircle width={300}/>
        <Card>
         
          <Card.Body>
            <Card.Text>
            Welcome to Knowledge Islands. This is an open-source software that analyzes developers' knowledge distribution in GitHub repositories. This tool implements source code knowledge models published in the following studies.
              <br /><br />
              <ul>
                <li><i>Cury, Otávio, et al. "Identifying Source Code File Experts." Proceedings of the 16th ACM/IEEE International Symposium on Empirical Software Engineering and Measurement. 2022.</i></li>
                <li><i>Cury, Otávio, et al. "Source code expert identification: Models and application." Information and Software Technology (2024): 107445.</i></li>
                <li><i>Cury, Otávio, and Guilherme Avelino. "Knowledge Islands: Visualizing Developers Knowledge Concentration." arXiv preprint arXiv:2408.08733 (2024).</i></li>
              </ul>
              This tool is being developed in <a href="https://github.com/OtavioCury/knowledge-islands">github.com/OtavioCury/knowledge-islands</a>
            </Card.Text>
          </Card.Body>
        </Card>
      </div>
    </>
  )
};

export default Index;