@Component
public class ActiveUsersActuatorEndpoint extends AbstractMvcEndpoint {

    //@Autowired
    //private ActiveUsersService activeUsersService;

    public ActiveUsersActuatorEndpoint() {
        super("/activeusers", false /* sensitive */);
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ActiveUsersResponse listActiveUsers() {
//        return new ActiveUsersResponse("Active users right now", activeUsersService.listActiveUsers());
        return new ActiveUsersResponse("Active users right now", Arrays.asList("Tom", "Dick", "Harry"));
    }

    @JsonPropertyOrder({"info", "activeUsers"})
    public static class ActiveUsersResponse {

        @JsonProperty
        private String info;

        @JsonProperty
        private List<String> activeUsers;

        public ActiveUsersResponse(String info, List<String> activeUsers) {
            this.info = info;
            this.activeUsers = activeUsers;
        }
    }
}
