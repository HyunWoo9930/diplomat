package publicdata.hackathon.diplomats.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		Info info = new Info()
			.title("Diplomats API")
			.version("1.0")
			.description("""
				🌍 **공공외교 및 ODA 플랫폼 API**
				
				외교실천일지, 투표, 커뮤니티 등 다양한 공공외교 기능을 제공하는 API입니다.
				
				**주요 기능:**
				- 🔐 사용자 인증 및 관리
				- 💬 커뮤니티 (자유게시판, 토론게시판, 외교실천일지)
				- 🗳️ 투표 시스템 (월별 투표, ODA 투표)
				- 🌍 ODA 프로젝트 및 공공외교 프로그램 관리
				- 📰 뉴스 및 보도자료 서비스
				- 📊 시민의식 테스트
				- 👍 좋아요 및 스크랩 기능
				""")
			.contact(new Contact()
				.name("Diplomats Team")
				.email("hw62459930@gmail.com"));

		return new OpenAPI()
			.info(info)
			.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
			.components(new io.swagger.v3.oas.models.Components()
				.addSecuritySchemes("Bearer Authentication", new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")))
			.servers(List.of(
				new Server().url("http://localhost:8080").description("로컬 개발 서버"),
				new Server().url("https://api.diplomats.com").description("운영 서버")
			))
			.tags(List.of(
				new Tag().name("📱 메인").description("메인페이지 및 기본 API"),
				new Tag().name("🔐 인증/사용자").description("회원가입, 로그인 및 사용자 관리"),
				new Tag().name("💬 커뮤니티").description("자유게시판, 토론게시판, 외교실천일지"),
				new Tag().name("🗳️ 투표").description("월별 투표 및 ODA 투표 시스템"),
				new Tag().name("🌍 ODA/공공외교").description("ODA 프로젝트 및 공공외교 프로그램"),
				new Tag().name("📰 뉴스/보도자료").description("외교부 뉴스 및 보도자료"),
				new Tag().name("📊 테스트/설문").description("시민의식 테스트"),
				new Tag().name("👍 좋아요/스크랩").description("좋아요 및 스크랩 기능"),
				new Tag().name("👤 마이페이지").description("내 게시글 및 개인 정보 관리")
			));
	}
}
